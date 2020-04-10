package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.VideoQuality
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

@Suppress("BlockingMethodInNonBlockingContext")
class TrackerServiceTest : BehaviorSpec({

    Given("a tv show that is not registered and has episode files available") {

        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = listOf(
                anyEpisodeFile(),
                anyEpisodeFile()
            )
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider)
        val service = startedService<TrackerService>(services)

        When("it is registered") {
            val recorder = recordEvents(services)
            service.register(tvShow.tvShow.id, VideoQuality.SD)

            Then("an event is fired for each episode file") {
                assert(recorder.waitForEvents(tvShow.episodeFiles.size))
                    .hasSize(tvShow.episodeFiles.size)
            }
        }
    }

    Given("a tv show that is registered") {

        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = emptyList()
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider)
        val service = startedService<TrackerService>(services)
        service.register(tvShow.tvShow.id, VideoQuality.default())

        When("a new episode file becomes available") {
            val recorder = recordEvents(services)
            provider.addEpisode(tvShow.tvShow, anyEpisodeFile(tvShow.tvShow))

            Then("an event is fired") {
                assert(recorder.waitForEvent())
                    .isNotNull()
            }
        }
    }

    Given("a tv show that has been unregistered") {

        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = emptyList()
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider)
        val service = startedService<TrackerService>(services)
        service.register(tvShow.tvShow.id, VideoQuality.default())
        service.unregister(tvShow.tvShow.id)

        When("a new episode file becomes available") {
            val recorder = recordEvents(services)
            provider.addEpisode(tvShow.tvShow, anyEpisodeFile(tvShow.tvShow))

            Then("no event is fired") {
                assert(recorder.waitForEvent())
                    .isNull()
            }
        }
    }

    Given("two tracked tv shows A and B") {

        val tvShowA = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = emptyList()
        )
        val tvShowB = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = emptyList()
        )
        val provider = fakeTVShowProvider(tvShowA, tvShowB)
        val services = services(provider)
        val service = startedService<TrackerService>(services)
        service.register(tvShowA.tvShow.id, VideoQuality.default())
        service.register(tvShowB.tvShow.id, VideoQuality.default())

        When("a new episode becomes available for tv show A") {
            val recorder = recordEvents(services)
            val newEpisode = anyEpisodeFile(tvShowA.tvShow)
            provider.addEpisode(tvShowA.tvShow, newEpisode)

            Then("no event is fired for tv show B") {
                assert(recorder.waitForEvents(2).map { it.episode })
                    .containsExactly(newEpisode)
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
