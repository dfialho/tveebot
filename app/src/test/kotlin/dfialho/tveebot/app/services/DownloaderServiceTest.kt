package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.fire
import dfialho.tveebot.downloader.api.DownloadEngine
import io.kotest.core.spec.style.FunSpec
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.time.Duration

@Suppress("BlockingMethodInNonBlockingContext")
class DownloaderServiceTest : FunSpec({

    val services by beforeTestSetup { services() }
    val service by beforeTestSetup { start<DownloaderService>(services) }

    fun submitNewEpisodeFile(episodeFile: EpisodeFile) {

        withRepository(services) {
            upsert(TVShowEntity(episodeFile.tvShow, tracked = true))
            insert(episodeFile)
        }

        val eventBus by services.instance<EventBus>()
        fire(eventBus, Event.EpisodeFileFound(episodeFile))
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

    test("when it starts up it starts downloading past files") {

        val newEpisodeFile = anyEpisodeFile(anyTVShow())

        submitNewEpisodeFile(newEpisodeFile)
        val recorder = recordEvents<Event.DownloadStarted>(services)
        service.stop()
        service.start()

        assert(recorder.waitForEvents(1))
            .hasSize(1)
    }

    test("when it starts up it does not download files that were not download before") {

        val episodeFile = anyEpisodeFile()
        withRepository(services) {
            upsert(TVShowEntity(episodeFile.tvShow, tracked = true))
            insert(episodeFile)
        }

        val recorder = recordEvents<Event.DownloadStarted>(services)
        service.stop()
        service.start()

        assert(recorder.waitForEvent())
            .isNull()
    }

    test("when it starts up it does not download files that have been downloaded") {

        val newEpisodeFile = anyEpisodeFile(anyTVShow())
        val engine by services.instance<FakeDownloadEngine>()

        submitNewEpisodeFile(newEpisodeFile)
        engine.finish(newEpisodeFile)
        val recorder = recordEvents<Event.DownloadStarted>(services)
        service.stop()
        service.start()

        assert(recorder.waitForEvent())
            .isNull()
    }

    test("even if a download was triggered twice on startup it is started once") {

        val newEpisodeFile = anyEpisodeFile(anyTVShow())

        submitNewEpisodeFile(newEpisodeFile)
        submitNewEpisodeFile(newEpisodeFile)
        val recorder = recordEvents<Event.DownloadStarted>(services)
        service.stop()
        service.start()

        assert(recorder.waitForEvents(2, Duration.ZERO))
            .hasSize(1)
    }
})

private fun services() = Kodein {
    import(downloaderModule)
    bind<Database>() with singleton { randomInMemoryDatabase() }

    val engine = FakeDownloadEngine()
    bind<DownloadEngine>(overrides = true) with singleton { engine }
    bind<FakeDownloadEngine>() with singleton { engine }
}
