package dfialho.tveebot.app.services.downloader

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import dfialho.tveebot.app.temporaryDirectory
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files

@Suppress("BlockingMethodInNonBlockingContext")
class DownloadCleanerTest : FunSpec({

    val downloadsDirectory by temporaryDirectory()

    test("when the download path is a video file then a path to itself is returned") {

        val cleaner = DownloadCleaner(DownloadScanner())
        val downloadFile = Files.createFile(downloadsDirectory.resolve("video.mkv"))

        val resultPath = cleaner.cleanUp(downloadFile)

        assert(resultPath)
            .isEqualTo(downloadFile)
    }

    test("when the download path is a directory with a video file then directory is replace by video file") {

        val cleaner = DownloadCleaner(DownloadScanner())
        val directory = Files.createDirectory(downloadsDirectory.resolve("videos"))
        Files.createFile(directory.resolve("video.mkv"))

        val resultPath = cleaner.cleanUp(directory)

        assertAll {
            assert(resultPath)
                .isEqualTo(downloadsDirectory.resolve("video.mkv"))

            assert(Files.exists(directory))
                .isFalse()
        }
    }

    test("when the video file is in the downloads directory then ") {

    }

})
