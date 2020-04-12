package dfialho.tveebot.app.services.downloader

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dfialho.tveebot.app.temporaryDirectory
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files

@Suppress("BlockingMethodInNonBlockingContext")
class DownloadScannerTest : FunSpec({

    val downloadDirectory by temporaryDirectory()
    val scanner = DownloadScanner()

    test("when there is no video file then null is returned") {

        Files.createFile(downloadDirectory.resolve("file.txt"))

        assert(scanner.scan(downloadDirectory))
            .isNull()
    }

    test("when there is one video file then that file is returned") {

        val videoFile = Files.createFile(downloadDirectory.resolve("video.mkv"))
        Files.createFile(downloadDirectory.resolve("file.txt"))

        assert(scanner.scan(downloadDirectory))
            .isEqualTo(videoFile)
    }

    test("when there is two video files with different sizes then the largest is returned") {

        val smallVideoFile = Files.createFile(downloadDirectory.resolve("small.mkv"))
        val bigVideoFile = Files.createFile(downloadDirectory.resolve("big.mkv"))
        Files.write(smallVideoFile, ByteArray(1))
        Files.write(bigVideoFile, ByteArray(10))

        assert(scanner.scan(downloadDirectory))
            .isEqualTo(bigVideoFile)
    }

    test("when there is two files where the non-video is the largest then the video file is returned") {

        val videoFile = Files.createFile(downloadDirectory.resolve("b.mkv"))
        val textFile = Files.createFile(downloadDirectory.resolve("a.txt"))
        Files.write(videoFile, ByteArray(1))
        Files.write(textFile, ByteArray(10))

        assert(scanner.scan(downloadDirectory))
            .isEqualTo(videoFile)
    }

    test("when there is a directory that looks like a video file then it is ignored") {

        Files.createDirectory(downloadDirectory.resolve("video.mkv"))

        assert(scanner.scan(downloadDirectory))
            .isNull()
    }

    test("when there is a video file with extension MKV then it is returned") {

        val videoFile = Files.createFile(downloadDirectory.resolve("video.mkv"))

        assert(scanner.scan(downloadDirectory))
            .isEqualTo(videoFile)
    }

    test("when there is a video file with extension AVI then it is returned") {

        val videoFile = Files.createFile(downloadDirectory.resolve("video.avi"))

        assert(scanner.scan(downloadDirectory))
            .isEqualTo(videoFile)
    }

    test("when there is a video file with extension MP4 then it is returned") {

        val videoFile = Files.createFile(downloadDirectory.resolve("video.mp4"))

        assert(scanner.scan(downloadDirectory))
            .isEqualTo(videoFile)
    }

    test("when there is a video file with extension mKv then it is returned") {

        val videoFile = Files.createFile(downloadDirectory.resolve("video.mKv"))

        assert(scanner.scan(downloadDirectory))
            .isEqualTo(videoFile)
    }
})
