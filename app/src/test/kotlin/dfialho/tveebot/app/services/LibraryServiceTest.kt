package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.anyEpisodeFile
import dfialho.tveebot.app.api.models.anyTVShow
import dfialho.tveebot.app.beforeTestSetup
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.fire
import dfialho.tveebot.app.recordEvents
import dfialho.tveebot.app.start
import dfialho.tveebot.library.api.TVShowLibrary
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.nio.file.Path
import java.nio.file.Paths

class LibraryServiceTest : FunSpec({

    val services by beforeTestSetup { services() }
    beforeTest { start<LibraryService>(services) }

    fun submitDownloadedEpisodeFile(episodeFile: EpisodeFile, downloadPath: Path) {

        val eventBus by services.instance<EventBus>()
        fire(eventBus, Event.FileStashed(episodeFile, downloadPath))
    }

    test("when an episode file is downloaded then a stored event is fired") {

        val newEpisodeFile = anyEpisodeFile()
        val recorder = recordEvents<Event.FileStored>(services)

        submitDownloadedEpisodeFile(newEpisodeFile, Paths.get("video.mkv"))

        assert(recorder.waitForEvent()?.episode)
            .isEqualTo(newEpisodeFile)
    }

    test("when the library fails to store the file then no event is fired") {

        val newEpisodeFile = anyEpisodeFile()
        val recorder = recordEvents<Event.FileStored>(services)
        val libraryMock by services.instance<TVShowLibrary>()
        every { libraryMock.store(any(), any()) } throws Exception()

        submitDownloadedEpisodeFile(newEpisodeFile, Paths.get("video.mkv"))

        assert(recorder.waitForEvent()?.episode)
            .isNull()
    }

    test("after the library fails to store the file new episode files are still stored") {

        val tvShow = anyTVShow()
        val firstEpisodeFile = anyEpisodeFile(tvShow)
        val secondEpisodeFile = anyEpisodeFile(tvShow)
        val recorder = recordEvents<Event.FileStored>(services)
        val libraryMock by services.instance<TVShowLibrary>()
        every { libraryMock.store(firstEpisodeFile, any()) } throws Exception("Some error occurred")
        every { libraryMock.store(secondEpisodeFile, any()) } returns Paths.get("library/video.mkv")

        submitDownloadedEpisodeFile(firstEpisodeFile, Paths.get("video1.mkv"))
        submitDownloadedEpisodeFile(secondEpisodeFile, Paths.get("video2.mkv"))

        assert(recorder.waitForEvent()?.episode)
            .isEqualTo(secondEpisodeFile)
    }
})

private fun services() = Kodein {
    import(libraryModule)
    bind<TVShowLibrary>(overrides = true) with singleton {
        mockk<TVShowLibrary> {
            every { store(any(), any()) } returns Paths.get("library/video.mkv")
        }
    }
}
