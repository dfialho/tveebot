package dfialho.tveebot.app.services.downloader

import com.google.common.primitives.Longs
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import com.google.common.io.Files as GFiles

@Suppress("UnstableApiUsage")
class DownloadScanner {

    companion object {
        private val VIDEO_EXTENSIONS = setOf("mkv", "avi", "mp4")
    }

    fun scan(downloadPath: Path): Path? {

        val filesStream = when {
            Files.isRegularFile(downloadPath) -> Stream.of(downloadPath)
            Files.isDirectory(downloadPath) -> Files.list(downloadPath)
            else -> null
        }

        return filesStream
            ?.filter { VIDEO_EXTENSIONS.contains(GFiles.getFileExtension(it.toString().toLowerCase())) }
            ?.filter { Files.isRegularFile(it) }
            ?.max { file1, file2 -> Longs.compare(Files.size(file1), Files.size(file2)) }
            ?.orElse(null)
    }
}
