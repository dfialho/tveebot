package dfialho.tveebot.library.lib

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path


/**
 * Tests for the [EpisodeDownloadPackage].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class EpisodeDownloadPackageTests {

    @Test
    fun `episode from file corresponds to the file path`(@TempDir downloadDirectory: Path) {
        val videoFilePath = Files.createFile(downloadDirectory.resolve("video.mp4"))
        val episodePackage = EpisodeDownloadPackage(videoFilePath)

        assert(episodePackage.getEpisode()).isEqualTo(videoFilePath)
    }

    @Test
    fun `episode from directory containing a single video file corresponds to that video file`(@TempDir downloadDirectory: Path) {
        val videoFilePath = Files.createFile(downloadDirectory.resolve("video.mp4"))

        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert(episodePackage.getEpisode()).isEqualTo(videoFilePath)
    }

    @Test
    fun `episode from directory containing a multiple video file corresponds to largest video file`(@TempDir downloadDirectory: Path) {

        val smallFile = downloadDirectory.resolve("small-file")
        smallFile.toFile().writeText("Small")
        val largeFile = downloadDirectory.resolve("large-file")
        largeFile.toFile().writeText("Some text to make file bigger")
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert(episodePackage.getEpisode()).isEqualTo(largeFile)
    }

    @Test
    fun `episode from empty directory throws IllegalStateException`(@TempDir downloadDirectory: Path) {;
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert {
            episodePackage.getEpisode()
        }.thrownError {
            isInstanceOf(IllegalStateException::class)
        }
    }

    @Test
    fun `episode from non-existing path throws IllegalStateException`(@TempDir tempDir: Path) {
        val downloadDirectory: Path = tempDir.resolve("fake")
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert {
            episodePackage.getEpisode()
        }.thrownError {
            isInstanceOf(IllegalStateException::class)
        }
    }
}
