package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.fire
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import io.kotest.core.spec.style.FunSpec
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

@Suppress("BlockingMethodInNonBlockingContext")
class DownloaderServiceTest : FunSpec({

    test("when a new episode file is found and its video quality matches the tv show's it starts being downloaded") {

        val engine = FakeDownloadEngine()
        val services = services(engine)
        startedService<DownloaderService>(services)
        val repository by services.instance<TVeebotRepository>()

        val trackedTVShow = anyTVShow()
        repository.upsert(TVShowEntity(trackedTVShow, tracked = true, videoQuality = VideoQuality.FHD))
        val newEpisodeFile = anyEpisodeFile(tvShow = trackedTVShow, file = anyVideoFile(quality = VideoQuality.FHD))

        val recorder = recordEvents<Event.DownloadStarted>(services)
        repository.insert(newEpisodeFile)
        val eventBus by services.instance<EventBus>()
        fire(eventBus, Event.EpisodeFileFound(newEpisodeFile))

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isEqualTo(newEpisodeFile)

            val episode = repository.findEpisode(newEpisodeFile.episodes[0].id)
            assert(episode?.state)
                .isEqualTo(State.DOWNLOADING)
        }
    }

    test("when an episode file finishes downloading an event is fired and its state is updated") {

        val engine = FakeDownloadEngine()
        val services = services(engine)
        startedService<DownloaderService>(services)
        val repository by services.instance<TVeebotRepository>()
        val trackedTVShow = anyTVShow()
        val newEpisodeFile = anyEpisodeFile(tvShow = trackedTVShow)
        repository.upsert(TVShowEntity(trackedTVShow, tracked = true))
        repository.insert(newEpisodeFile)
        val eventBus by services.instance<EventBus>()
        fire(eventBus, Event.EpisodeFileFound(newEpisodeFile))

        val recorder = recordEvents<Event.DownloadFinished>(services)
        engine.finish(newEpisodeFile)

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isEqualTo(newEpisodeFile)

            val episode = repository.findEpisode(newEpisodeFile.episodes[0].id)
            assert(episode?.state)
                .isEqualTo(State.DOWNLOADED)
        }
    }

    test("when a new episode becomes available with another video quality it is NOT downloaded") {

        val engine = FakeDownloadEngine()
        val services = services(engine)
        val eventBus by services.instance<EventBus>()
        val repository by services.instance<TVeebotRepository>()
        startedService<DownloaderService>(services)

        val trackedTVShow = anyTVShow()
        repository.upsert(TVShowEntity(trackedTVShow, tracked = true, videoQuality = VideoQuality.FHD))

        val recorder = recordEvents<Event.DownloadStarted>(services)
        val newEpisodeFile = anyEpisodeFile(
            tvShow = trackedTVShow,
            file = anyVideoFile(quality = VideoQuality.SD)
        )
        fire(eventBus, Event.EpisodeFileFound(newEpisodeFile))

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isNull()

            val episode = repository.findEpisode(newEpisodeFile.episodes[0].id)
            assert(episode?.state)
                .isNotEqualTo(State.DOWNLOADING)
            assert(episode?.state)
                .isNotEqualTo(State.DOWNLOADED)
        }
    }
})

private fun services(engine: FakeDownloadEngine) = Kodein {
    import(downloaderModule)
    bind<TVeebotRepository>(overrides = true) with instance(newRepository())
    bind<DownloadEngine>(overrides = true) with singleton { engine }
}
