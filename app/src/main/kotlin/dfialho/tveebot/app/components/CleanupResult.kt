package dfialho.tveebot.app.components

import java.nio.file.Path

sealed class CleanupResult {
    data class Success(val path: Path) : CleanupResult()
    object UnsupportedFileType : CleanupResult()
    object PathNotExists : CleanupResult()
    object VideoFileNotFound : CleanupResult()
    class UnexpectedError(val exception: Exception) : CleanupResult()
}
