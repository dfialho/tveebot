package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.EpisodeFile
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

    fun submitNewEpisodeFile(newEpisodeFile: EpisodeFile) {

        val eventBus by services.instance<EventBus>()
        fire(eventBus, Event.EpisodeFileFound(newEpisodeFile))
    }

    test("when a new episode file is found it starts being downloaded") {

        val newEpisodeFile = anyEpisodeFile()
        val recorder = recordEvents<Event.DownloadStarted>(services)

        submitNewEpisodeFile(newEpisodeFile)

        assert(recorder.waitForEvent()?.episode)
            .isEqualTo(newEpisodeFile)
    }

    test("when an episode file finishes downloading an event is fired") {

        val newEpisodeFile = anyEpisodeFile()
        submitNewEpisodeFile(newEpisodeFile)
        val recorder = recordEvents<Event.DownloadFinished>(services)

        val engine by services.instance<FakeDownloadEngine>()
        engine.finish(newEpisodeFile)

        assert(recorder.waitForEvent()?.episode)
            .isEqualTo(newEpisodeFile)
    }
})

private fun services() = Kodein {
    import(downloaderModule)
    bind<TVeebotRepository>(overrides = true) with singleton { newRepository() }

    val engine = FakeDownloadEngine()
    bind<DownloadEngine>(overrides = true) with singleton { engine }
    bind<FakeDownloadEngine>() with singleton { engine }
}
