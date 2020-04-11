package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import io.kotest.core.spec.style.FunSpec
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.time.Duration

@Suppress("BlockingMethodInNonBlockingContext")
class TrackerServiceTest : FunSpec({

    val checkPeriod: Duration = Duration.ofMillis(10)

    fun services(tvShowProvider: TVShowProvider, trackerCheckPeriod: Duration? = null) = Kodein {
        import(trackerModule)
        bind<TVeebotRepository>(overrides = true) with singleton { newRepository() }
        bind<TVShowProvider>(overrides = true) with singleton { tvShowProvider }
        bind<TrackerEngine>(overrides = true) with singleton {
            ScheduledTrackerEngine(
                instance(),
                instance(),
                trackerCheckPeriod ?: checkPeriod
            )
        }
    }

    test("when a tv show is registered an event is fired for each episode available") {
        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = listOf(
                anyEpisodeFile(),
                anyEpisodeFile()
            )
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider)
        val service = start<TrackerService>(services)

        val recorder = recordEvents<Event.EpisodeFileFound>(services)
        service.register(tvShow.tvShow.id, VideoQuality.SD)

        assert(recorder.waitForEvents(tvShow.episodeFiles.size, checkPeriod.multipliedBy(10)))
            .hasSize(tvShow.episodeFiles.size)
    }

    test("when a new episode becomes available an event is fired") {

        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = emptyList()
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider)
        val service = start<TrackerService>(services)
        service.register(tvShow.tvShow.id, VideoQuality.default())

        val recorder = recordEvents<Event.EpisodeFileFound>(services)
        provider.addEpisode(tvShow.tvShow, anyEpisodeFile(tvShow.tvShow))

        assert(recorder.waitForEvent(checkPeriod.multipliedBy(10)))
            .isNotNull()
    }

    test("after a tv show has been unregistered any new episode file does not trigger an event") {

        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = emptyList()
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider)
        val service = start<TrackerService>(services)
        service.register(tvShow.tvShow.id, VideoQuality.default())
        service.unregister(tvShow.tvShow.id)

        val recorder = recordEvents<Event.EpisodeFileFound>(services)
        provider.addEpisode(tvShow.tvShow, anyEpisodeFile(tvShow.tvShow))

        assert(recorder.waitForEvent(checkPeriod.multipliedBy(3)))
            .isNull()
    }

    test("when a new episode becomes available for tv show A no event is fired for tv show B") {

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
        val service = start<TrackerService>(services)
        afterTest { service.stop() }

        service.register(tvShowA.tvShow.id, VideoQuality.default())
        service.register(tvShowB.tvShow.id, VideoQuality.default())

        val recorder = recordEvents<Event.EpisodeFileFound>(services)
        val newEpisode = anyEpisodeFile(tvShowA.tvShow)
        provider.addEpisode(tvShowA.tvShow, newEpisode)

        val waitForEvents = recorder.waitForEvents(2, checkPeriod.multipliedBy(3))

        assert(waitForEvents.map { it.episode })
            .containsExactly(newEpisode)
    }

    test("when registering a new tv show the available episodes are detected only once") {

        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = listOf(
                anyEpisodeFile(),
                anyEpisodeFile()
            )
        )
        val provider = fakeTVShowProvider(tvShow)
        val services = services(provider, trackerCheckPeriod = Duration.ofMillis(1))
        val service = start<TrackerService>(services)
        val eventBus by services.instance<EventBus>()
        val recorder = recordEvents<Event.EpisodeFileFound>(services)

        subscribe<Event.EpisodeFileFound>(eventBus) {
            Thread.sleep(checkPeriod.multipliedBy(3).toMillis())
        }

        service.register(tvShowId = tvShow.tvShow.id, videoQuality = VideoQuality.default())

        assert(recorder.waitForEvents(2, checkPeriod.multipliedBy(10)))
            .hasSize(2)
    }

})
