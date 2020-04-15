package dfialho.tveebot.app.services.downloader

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import dfialho.tveebot.app.components.CleanupResult
import dfialho.tveebot.app.components.DownloadCleaner
import dfialho.tveebot.app.components.DownloadScanner
import dfialho.tveebot.commons.temporaryDirectory
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files

@Suppress("BlockingMethodInNonBlockingContext")
class DownloadCleanerTest : FunSpec({

    val downloadsDirectory by temporaryDirectory()

    test("when the download path is a video file then a path to itself is returned") {

        val cleaner = DownloadCleaner(DownloadScanner())
        val downloadFile = Files.createFile(downloadsDirectory.resolve("video.mkv"))

        val result = cleaner.cleanUp(downloadFile)

        assert(result)
            .isInstanceOf(CleanupResult.Success::class)

        result as CleanupResult.Success

        assert(result.path)
            .isEqualTo(downloadFile)
    }

    test("when the download path is a directory with a video file then directory is replace by video file") {

        val cleaner = DownloadCleaner(DownloadScanner())
        val directory = Files.createDirectory(downloadsDirectory.resolve("videos"))
        Files.createFile(directory.resolve("video.mkv"))

        val result = cleaner.cleanUp(directory)

        assertAll {
            assert(result)
                .isInstanceOf(CleanupResult.Success::class)

            result as CleanupResult.Success

            assert(result.path)
                .isEqualTo(downloadsDirectory.resolve("video.mkv"))

            assert(Files.exists(directory))
                .isFalse()
        }
    }

    test("when the provided path does not exist then the result is ${CleanupResult.PathNotExists}") {

        val cleaner = DownloadCleaner(DownloadScanner())

        val result = cleaner.cleanUp(downloadsDirectory.resolve("video.mkv"))

        assert(result)
            .isInstanceOf(CleanupResult.PathNotExists::class)
    }

    test("when the provided path is not a video file then the result is ${CleanupResult.VideoFileNotFound}") {

        val cleaner = DownloadCleaner(DownloadScanner())
        val file = Files.createFile(downloadsDirectory.resolve("file.txt"))

        val result = cleaner.cleanUp(file)

        assert(result)
            .isInstanceOf(CleanupResult.VideoFileNotFound::class)
    }

    test("when the provided directory does not include any video file then the result is ${CleanupResult.VideoFileNotFound}") {

        val cleaner = DownloadCleaner(DownloadScanner())
        val directory = Files.createDirectory(downloadsDirectory.resolve("videos"))
        Files.createFile(directory.resolve("file.txt"))

        val result = cleaner.cleanUp(directory)

        assert(result)
            .isInstanceOf(CleanupResult.VideoFileNotFound::class)
    }
})
