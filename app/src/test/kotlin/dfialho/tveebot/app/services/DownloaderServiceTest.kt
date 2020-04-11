package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.TVShowEntity
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

    val services by beforeTestSetup { services() }
    beforeTest { start<DownloaderService>(services) }

    fun submitNewEpisodeFile(tvShow: TVShowEntity, newEpisodeFile: EpisodeFile) {

        withRepository(services) {
            upsert(tvShow)
            insert(newEpisodeFile)
        }

        val eventBus by services.instance<EventBus>()
        fire(eventBus, Event.EpisodeFileFound(newEpisodeFile))
    }

    test("when a new episode file is found it starts being downloaded") {

        val trackedTVShow = TVShowEntity(anyTVShow(), tracked = true)
        val newEpisodeFile = anyEpisodeFile(tvShow = trackedTVShow.tvShow)

        val recorder = recordEvents<Event.DownloadStarted>(services)
        submitNewEpisodeFile(trackedTVShow, newEpisodeFile)

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isEqualTo(newEpisodeFile)

            val episode = withRepository(services) { findEpisode(newEpisodeFile.episodes[0].id) }
            assert(episode?.state)
                .isEqualTo(State.DOWNLOADING)
        }
    }

    test("when an episode file finishes downloading an event is fired and its state is updated") {

        val trackedTVShow = TVShowEntity(anyTVShow(), tracked = true)
        val newEpisodeFile = anyEpisodeFile(tvShow = trackedTVShow.tvShow)

        val recorder = recordEvents<Event.DownloadFinished>(services)
        submitNewEpisodeFile(trackedTVShow, newEpisodeFile)

        val engine by services.instance<FakeDownloadEngine>()
        engine.finish(newEpisodeFile)

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isEqualTo(newEpisodeFile)

            val episode = withRepository(services) { findEpisode(newEpisodeFile.episodes[0].id) }
            assert(episode?.state)
                .isEqualTo(State.DOWNLOADED)
        }
    }
})

private fun services() = Kodein {
    import(downloaderModule)
    bind<TVeebotRepository>(overrides = true) with singleton { newRepository() }

    val engine = FakeDownloadEngine()
    bind<DownloadEngine>(overrides = true) with singleton { engine }
    bind<FakeDownloadEngine>() with singleton { engine }
}
