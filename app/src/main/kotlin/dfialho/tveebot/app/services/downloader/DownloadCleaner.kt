package dfialho.tveebot.app.services.downloader

import java.nio.file.Files
import java.nio.file.Path

class DownloadCleaner(private val scanner: DownloadScanner) {

    fun cleanUp(downloadPath: Path): Path {

        return when {
            Files.isRegularFile(downloadPath) -> downloadPath
            Files.isDirectory(downloadPath) -> extractVideoFile(downloadPath)
            Files.notExists(downloadPath) -> throw IllegalStateException("Path does not exist: $downloadPath")
            else -> throw IllegalStateException("Path is neither a directory or a regular file")
        }
    }

    private fun extractVideoFile(downloadDirectory: Path): Path {

        val videoFile = scanner.scan(downloadDirectory)
            ?: throw IllegalStateException("No video file found in $downloadDirectory")

        val finalPath = Files.move(videoFile, videoFile.parent.parent.resolve(videoFile.fileName))
        downloadDirectory.toFile().deleteRecursively()

        return finalPath
    }
}
