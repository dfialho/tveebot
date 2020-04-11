package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.fire
import dfialho.tveebot.app.repositories.TVeebotRepository
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

class OrganizerServiceTest : FunSpec({

    val services by beforeTestSetup { services() }
    beforeTest { start<OrganizerService>(services) }

    fun submitDownloadedEpisodeFile(tvShow: TVShowEntity, episodeFile: EpisodeFile, downloadPath: Path) {

        withRepository(services) {
            upsert(tvShow)
            insert(episodeFile)
        }

        val eventBus by services.instance<EventBus>()
        fire(eventBus, Event.DownloadFinished(episodeFile, downloadPath))
    }

    test("when an episode file is downloaded then a stored event is fired") {

        val trackedTVShow = TVShowEntity(anyTVShow(), tracked = true)
        val newEpisodeFile = anyEpisodeFile(
            tvShow = trackedTVShow.tvShow,
            file = anyVideoFile()
        )

        val recorder = recordEvents<Event.FileStored>(services)
        submitDownloadedEpisodeFile(trackedTVShow, newEpisodeFile, Paths.get("video.mkv"))

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isEqualTo(newEpisodeFile)

            val episode = withRepository(services) { findEpisode(newEpisodeFile.episodes[0].id) }
            assert(episode?.state)
                .isEqualTo(State.STORED)
        }
    }

    test("when the library fails to store the file then no event is fired"){

        val libraryMock by services.instance<TVShowLibrary>()
        every { libraryMock.store(any(), any()) } throws Exception()

        val trackedTVShow = TVShowEntity(anyTVShow(), tracked = true)
        val newEpisodeFile = anyEpisodeFile(
            tvShow = trackedTVShow.tvShow,
            file = anyVideoFile()
        )

        val recorder = recordEvents<Event.FileStored>(services)
        submitDownloadedEpisodeFile(trackedTVShow, newEpisodeFile, Paths.get("video.mkv"))

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isNull()

            val episode = withRepository(services) { findEpisode(newEpisodeFile.episodes[0].id) }
            assert(episode?.state)
                .isNotEqualTo(State.STORED)
        }

    }

    test("after the library fails to store the file new episode files are still stored"){

        val trackedTVShow = TVShowEntity(anyTVShow(), tracked = true)
        val firstEpisodeFile = anyEpisodeFile(
            tvShow = trackedTVShow.tvShow,
            file = anyVideoFile()
        )
        val secondEpisodeFile = anyEpisodeFile(
            tvShow = trackedTVShow.tvShow,
            file = anyVideoFile()
        )

        val libraryMock by services.instance<TVShowLibrary>()
        every { libraryMock.store(firstEpisodeFile.episodes, any()) } throws Exception("Some error occurred")
        every { libraryMock.store(secondEpisodeFile.episodes, any()) } returns Paths.get("library/video.mkv")

        val recorder = recordEvents<Event.FileStored>(services)
        submitDownloadedEpisodeFile(trackedTVShow, firstEpisodeFile, Paths.get("video1.mkv"))
        submitDownloadedEpisodeFile(trackedTVShow, secondEpisodeFile, Paths.get("video2.mkv"))

        assertAll {
            assert(recorder.waitForEvent()?.episode)
                .isEqualTo(secondEpisodeFile)

            val firstEpisode = withRepository(services) { findEpisode(firstEpisodeFile.episodes[0].id) }
            assert(firstEpisode?.state)
                .isNotEqualTo(State.STORED)

            val secondEpisode = withRepository(services) { findEpisode(secondEpisodeFile.episodes[0].id) }
            assert(secondEpisode?.state)
                .isEqualTo(State.STORED)
        }
    }
})

private fun services() = Kodein {
    import(organizerModule)
    bind<TVeebotRepository>(overrides = true) with singleton { newRepository() }
    bind<TVShowLibrary>(overrides = true) with singleton {
        mockk<TVShowLibrary> {
            every { store(any(), any()) } returns Paths.get("library/video.mkv")
        }
    }
}
