package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.hasSize
import dfialho.tveebot.app.anyEpisodeFile
import dfialho.tveebot.app.anyTVShow
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.events.unsubscribe
import dfialho.tveebot.app.newRepository
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import io.kotest.core.spec.style.BehaviorSpec
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Suppress("BlockingMethodInNonBlockingContext")
class TrackerServiceTest : BehaviorSpec({

    val safetyPeriod = 1000L

    Given("a tv show with 2 new episode files") {
        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = listOf(
                anyEpisodeFile(),
                anyEpisodeFile()
            )
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider)
        val eventBus by services.instance<EventBus>()
        val service by services.instance<TrackerService>()
        service.start()

        val registrationLatch = CountDownLatch(tvShow.episodeFiles.size)
        val firedEvents = mutableListOf<Event.EpisodeFileFound>()
        subscribe<Event.EpisodeFileFound>(eventBus) {
            firedEvents.add(it)
            registrationLatch.countDown()
        }

        When("it starts being tracked") {
            service.register(tvShow.tvShow.id, VideoQuality.default())

            Then("eventually an ${Event.EpisodeFileFound::class.simpleName} event is fired for each episode file") {
                registrationLatch.await(safetyPeriod, TimeUnit.MILLISECONDS)
                assert(firedEvents)
                    .hasSize(tvShow.episodeFiles.size)
            }

            And("no event is triggered while there is no new episode file") {
                Thread.sleep(100)
                assert(firedEvents)
                    .hasSize(tvShow.episodeFiles.size)
            }
        }

        unsubscribe<Event.EpisodeFileFound>(eventBus)
        firedEvents.clear()
        val newEpisodeLatch = CountDownLatch(1)
        subscribe<Event.EpisodeFileFound>(eventBus) {
            firedEvents.add(it)
            newEpisodeLatch.countDown()
        }

        When("a new episode file becomes available") {
            provider.addEpisode(tvShow.tvShow, anyEpisodeFile(tvShow.tvShow))

            Then("eventually an ${Event.EpisodeFileFound::class.simpleName} event is fired for that episode file") {
                newEpisodeLatch.await(safetyPeriod, TimeUnit.MILLISECONDS)
                assert(firedEvents)
                    .hasSize(1)
            }

            And("no event is triggered while there is no new episode file") {
                Thread.sleep(100)
                assert(firedEvents)
                    .hasSize(1)
            }
        }
    }
})

private fun services(tvShowProvider: TVShowProvider) = Kodein {
    import(trackerModule)
    bind<TVeebotRepository>(overrides = true) with instance(newRepository())
    bind<TVShowProvider>(overrides = true) with instance(tvShowProvider)
    bind<TrackerEngine>(overrides = true) with singleton {
        ScheduledTrackerEngine(
            instance(),
            instance(),
            Duration.ofMillis(10)
        )
    }
}
