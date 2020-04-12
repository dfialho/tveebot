package dfialho.tveebot.app.services.downloader

import com.google.common.primitives.Longs
import java.nio.file.Files
import java.nio.file.Path
import com.google.common.io.Files as GFiles

@Suppress("UnstableApiUsage")
class DownloadScanner {

    companion object {
        private val VIDEO_EXTENSIONS = setOf("mkv", "avi", "mp4")
    }

    fun scan(downloadDirectory: Path): Path? {
        require(Files.isDirectory(downloadDirectory)) { "Path must be a directory" }

        return Files.list(downloadDirectory)
            .filter { VIDEO_EXTENSIONS.contains(GFiles.getFileExtension(it.toString().toLowerCase())) }
            .filter { Files.isRegularFile(it) }
            .max { file1, file2 -> Longs.compare(Files.size(file1), Files.size(file2)) }
            .orElse(null)
    }
}
