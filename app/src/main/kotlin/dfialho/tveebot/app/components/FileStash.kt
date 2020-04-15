package dfialho.tveebot.app.components

import dfialho.tveebot.app.repositories.FileStashRepository
import mu.KLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class FileStash(
    private val stashDirectory: Path,
    private val repository: FileStashRepository
) {
    companion object : KLogging()

    data class StashedFile(val name: String, val fileId: String)

    fun put(fileId: String, videoFile: Path): Path {

        Files.createDirectories(stashDirectory)
        val path = Files.move(videoFile, stashDirectory.resolve(videoFile.fileName), StandardCopyOption.REPLACE_EXISTING)
        repository.insert(StashedFile(videoFile.fileName.toString(), fileId))

        return path
    }

    fun takeEach(block: (String, Path) -> Unit) {

        repository.list().forEach { (filename, fileId) ->

            val path = stashDirectory.resolve(filename)

            if (Files.exists(path)) {
                block(fileId, path)
                repository.remove(name = filename)
            } else {
                logger.warn { "Skipping file '$filename' because it was not found in stash directory" }
            }
        }
    }
}
