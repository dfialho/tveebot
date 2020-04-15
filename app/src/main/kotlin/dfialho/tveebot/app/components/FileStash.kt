package dfialho.tveebot.app.components

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.repositories.FileStashRepository
import mu.KLogging
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class FileStash(
    private val stashDirectory: Path,
    private val repository: FileStashRepository
) {

    companion object : KLogging()

    data class StashedFile(val name: String, val episodeFile: EpisodeFile)

    fun put(videoFile: Path, episodeFile: EpisodeFile): String {

        Files.move(videoFile, stashDirectory.resolve(videoFile.fileName), StandardCopyOption.REPLACE_EXISTING)
        repository.insert(StashedFile(videoFile.fileName.toString(), episodeFile))

        return videoFile.fileName.toString()
    }

    fun take(fileReference: String, block: (Path, EpisodeFile) -> Unit) {

        val stashedFile = repository.findByName(fileReference)
            ?: throw FileNotFoundException("File with reference '$fileReference' does not exist in the stash")

        val path = stashDirectory.resolve(stashedFile.name)

        try {
            block(path, stashedFile.episodeFile)
            repository.remove(name = fileReference)

        } catch (e: Throwable) {
            logger.error(e) { "Failed to take video file from stash: $path" }
            throw e
        }
    }

    fun takeEach(block: (Path, EpisodeFile) -> Unit) {

        return repository.list().forEach {
            take(it.name, block)
        }
    }
}
