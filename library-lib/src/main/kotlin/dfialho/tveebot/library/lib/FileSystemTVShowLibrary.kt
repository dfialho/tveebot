package dfialho.tveebot.library.lib

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowOrganizer
import mu.KLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import com.google.common.io.Files as GFiles

@Suppress("UnstableApiUsage")
class FileSystemTVShowLibrary(private val libraryDirectory: Path, private val organizer: TVShowOrganizer) : TVShowLibrary {

    companion object : KLogging()

    override fun store(episodeFile: EpisodeFile, videoFile: Path): Path {

        val targetPath = organizer.getLocationOf(episodeFile)
        val extension = GFiles.getFileExtension(videoFile.toString())
        val destinationSubPath = targetPath.resolveSibling("${targetPath.fileName}.$extension")
        val destinationPath = libraryDirectory.resolve(destinationSubPath)

        logger.debug { "Storing episodes ${episodeFile.episodes} in $destinationPath" }
        Files.createDirectories(destinationPath.parent)
        Files.move(videoFile, destinationPath, StandardCopyOption.REPLACE_EXISTING)

        return destinationPath
    }
}