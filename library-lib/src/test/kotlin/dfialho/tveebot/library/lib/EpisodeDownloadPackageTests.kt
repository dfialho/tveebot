package dfialho.tveebot.library.lib

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KProperty


/**
 * Tests for the [EpisodeDownloadPackage].
 */
@Suppress("BlockingMethodInNonBlockingContext")
class EpisodeDownloadPackageTests : FunSpec({

    class TemporaryDirectory : AutoCloseable {

        val directory = createTempDir()

        override fun close() {
            directory.deleteRecursively()
        }

        operator fun getValue(thisRef: Nothing?, property: KProperty<*>): Path {
            return directory.toPath()
        }

    }

    lateinit var tempDir: TemporaryDirectory

    beforeTest {
        tempDir = TemporaryDirectory()
    }

    afterTest {
        tempDir.close()
    }

    test("episode from file corresponds to the file path") {

        val downloadDirectory by tempDir
        val videoFilePath = Files.createFile(downloadDirectory.resolve("video.mp4"))
        val episodePackage = EpisodeDownloadPackage(videoFilePath)

        assert(episodePackage.getEpisode()).isEqualTo(videoFilePath)
    }

    test("episode from directory containing a single video file corresponds to that video file") {

        val downloadDirectory by tempDir
        val videoFilePath = Files.createFile(downloadDirectory.resolve("video.mp4"))

        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert(episodePackage.getEpisode()).isEqualTo(videoFilePath)
    }

    test("episode from directory containing a multiple video file corresponds to largest video file") {

        val downloadDirectory by tempDir
        val smallFile = downloadDirectory.resolve("small-file")
        smallFile.toFile().writeText("Small")
        val largeFile = downloadDirectory.resolve("large-file")
        largeFile.toFile().writeText("Some text to make file bigger")
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert(episodePackage.getEpisode()).isEqualTo(largeFile)
    }

    test("episode from empty directory throws an exception") {

        val downloadDirectory by tempDir
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert {
            episodePackage.getEpisode()
        }.thrownError {
            isInstanceOf(IllegalStateException::class)
        }
    }

    test("episode from non-existing path throws an exception") {

        val directory by tempDir
        val downloadDirectory: Path = directory.resolve("fake")
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert {
            episodePackage.getEpisode()
        }.thrownError {
            isInstanceOf(IllegalStateException::class)
        }
    }
})
