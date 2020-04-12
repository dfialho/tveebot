package dfialho.tveebot.library.lib

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.anyEpisode
import dfialho.tveebot.app.api.models.anyEpisodeFile
import dfialho.tveebot.commons.temporaryDirectory
import dfialho.tveebot.library.api.TVShowOrganizer
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files
import java.nio.file.Path

@Suppress("BlockingMethodInNonBlockingContext")
class FileSystemTVShowLibraryTest : FunSpec({

    val downloadsDirectory by temporaryDirectory()
    val libraryDirectory by temporaryDirectory()
    val organizer = PlexTVShowOrganizer()

    test("store an episode file") {

        val library = FileSystemTVShowLibrary(libraryDirectory, organizer)
        val episodeFile = anyEpisodeFile(anyEpisode(season = 1, number = 2))
        val videoFile = Files.createFile(downloadsDirectory.resolve("video.mkv"))

        val storedPath = library.store(episodeFile, videoFile)

        assertAll {
            assert(libraryDirectory.relativize(storedPath))
                .isEqualTo(organizer.locationWithExtension(episodeFile))

            assert(Files.exists(videoFile))
                .isFalse()

            assert(Files.exists(storedPath))
                .isTrue()
        }
    }

    test("store a file that already exists") {

        val library = FileSystemTVShowLibrary(libraryDirectory, organizer)
        val episodeFile = anyEpisodeFile(anyEpisode(season = 1, number = 2))

        val firstVideoFile = Files.write(downloadsDirectory.resolve("video.mkv"), listOf("first"))
        library.store(episodeFile, firstVideoFile)
        val secondFileContent = listOf("second")
        val secondVideoFile = Files.write(downloadsDirectory.resolve("video.mkv"), secondFileContent)

        val storedPath = library.store(episodeFile, secondVideoFile)

        assertAll {
            assert(Files.readAllLines(storedPath))
                .isEqualTo(secondFileContent)

            assert(Files.exists(firstVideoFile))
                .isFalse()

            assert(Files.exists(storedPath))
                .isTrue()
        }
    }

    test("when the library directory does not exist it is created") {

        val realLibraryDirectory = libraryDirectory.resolve("library")
        val library = FileSystemTVShowLibrary(realLibraryDirectory, organizer)
        val episodeFile = anyEpisodeFile(anyEpisode(season = 1, number = 2))
        val videoFile = Files.createFile(downloadsDirectory.resolve("video.mkv"))

        val storedPath = library.store(episodeFile, videoFile)

        assertAll {
            assert(realLibraryDirectory.relativize(storedPath))
                .isEqualTo(organizer.locationWithExtension(episodeFile))

            assert(Files.exists(videoFile))
                .isFalse()

            assert(Files.exists(storedPath))
                .isTrue()
        }
    }
})

private fun TVShowOrganizer.locationWithExtension(episodeFile: EpisodeFile): Path {

    return this.locationOf(episodeFile).let {
        it.parent.resolve("${it.fileName}.mkv")
    }
}
