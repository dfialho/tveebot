package dfialho.tveebot.app.services.downloader

import java.nio.file.Files
import java.nio.file.Path

class DownloadCleaner(private val scanner: DownloadScanner) {

    fun cleanUp(downloadPath: Path): CleanupResult {

        return when {
            Files.isRegularFile(downloadPath) -> extractVideoFile(downloadPath)
            Files.isDirectory(downloadPath) -> extractVideoFileFromDirectory(downloadPath)
            Files.notExists(downloadPath) -> CleanupResult.PathNotExists
            else -> CleanupResult.UnsupportedFileType
        }
    }

    private fun extractVideoFile(downloadFile: Path): CleanupResult {

        val videoFile = scanner.scan(downloadFile)

        return if (videoFile == null) {
            CleanupResult.VideoFileNotFound
        } else {
            CleanupResult.Success(downloadFile)
        }
    }

    private fun extractVideoFileFromDirectory(downloadDirectory: Path): CleanupResult {

        val videoFile = scanner.scan(downloadDirectory)

        return if (videoFile == null) {
            CleanupResult.VideoFileNotFound
        } else {
            val finalPath = Files.move(videoFile, videoFile.parent.parent.resolve(videoFile.fileName))
            downloadDirectory.toFile().deleteRecursively()
            CleanupResult.Success(finalPath)
        }
    }
}
