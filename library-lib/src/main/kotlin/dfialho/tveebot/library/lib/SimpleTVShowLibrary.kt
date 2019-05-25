package dfialho.tveebot.library.lib

import dfialho.tveebot.library.api.EpisodePackage
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowOrganizer
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import mu.KLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import com.google.common.io.Files as GFiles

class SimpleTVShowLibrary(private val libraryDirectory: Path, private val organizer: TVShowOrganizer) : TVShowLibrary {

    companion object : KLogging()

    override fun store(episode: TVShowEpisode, episodePackage: EpisodePackage) {

        val episodeLocation = organizer.getLocationOf(episode)
        val episodePath = episodePackage.getEpisode()
        val extension = GFiles.getFileExtension(episodePath.fileName.toString())
        val destinationSubPath = episodeLocation.resolveSibling("${episodeLocation.fileName}.$extension")
        val destinationPath = libraryDirectory.resolve(destinationSubPath)

        logger.debug { "Storing episode $episode in ${destinationPath.toAbsolutePath()}" }
        Files.createDirectories(destinationPath.parent)
        Files.move(episodePackage.getEpisode(), destinationPath, StandardCopyOption.REPLACE_EXISTING)

        logger.debug { "Cleanup episode package: ${episodePackage.path}" }
        episodePackage.path.toFile().deleteRecursively()
    }
}