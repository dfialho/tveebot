package dfialho.tveebot.app.services.downloader

import mu.KLogging
import java.nio.file.Files
import java.nio.file.Path

class DownloadCleaner(private val scanner: DownloadScanner) {

    companion object : KLogging()

    fun cleanUp(downloadPath: Path): CleanupResult {

        return try {
            when {
                Files.isRegularFile(downloadPath) -> extractVideoFile(downloadPath)
                Files.isDirectory(downloadPath) -> extractVideoFileFromDirectory(downloadPath)
                Files.notExists(downloadPath) -> CleanupResult.PathNotExists
                else -> CleanupResult.UnsupportedFileType
            }

        } catch (e: Exception) {
            CleanupResult.UnexpectedError(e)
        }
    }

    private fun extractVideoFile(downloadFile: Path): CleanupResult {

        logger.debug { "Scanning video file: $downloadFile" }

        val videoFile = scanner.scan(downloadFile)

        return if (videoFile == null) {
            CleanupResult.VideoFileNotFound
        } else {
            CleanupResult.Success(downloadFile)
        }
    }

    private fun extractVideoFileFromDirectory(downloadDirectory: Path): CleanupResult {

        logger.debug { "Extracting video file from directory: $downloadDirectory" }

        return scanner.scan(downloadDirectory)
            ?.let { moveUp(it) }
            ?.let { CleanupResult.Success(it) }
            ?.also { delete(downloadDirectory) }
            ?: CleanupResult.VideoFileNotFound
    }

    private fun delete(downloadDirectory: Path) {
        logger.debug { "Deleting download directory: $downloadDirectory" }
        downloadDirectory.toFile().deleteRecursively()
    }

    private fun moveUp(videoFile: Path): Path {
        val targetPath = videoFile.parent.parent.resolve(videoFile.fileName)
        logger.debug { "Moving '$videoFile' to '$targetPath'" }
        return Files.move(videoFile, targetPath)
    }
}
