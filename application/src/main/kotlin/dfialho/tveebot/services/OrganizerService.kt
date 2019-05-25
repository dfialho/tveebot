package dfialho.tveebot.services

import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.lib.EpisodeDownloadPackage
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import mu.KLogging
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OrganizerService(private val library: TVShowLibrary) : Service {
    companion object : KLogging()

    override val name: String
        get() = "Organizer Service"

    private val executor = Executors.newSingleThreadExecutor()

    override fun start() = logStart(logger)

    override fun stop() = logStop(logger) {
        with(executor) {
            shutdown()

            try {
                awaitTermination(30, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                logger.warn { "Failed to stop Organizer Service cleanly: Timed out waiting for task to finish." }
            }
        }
    }

    fun store(episode: TVShowEpisodeFile, episodeCurrentLocation: Path) {
        executor.submit {
            try {
                logger.debug { "Storing episode in library: ${episode.toPrettyString()}" }
                library.store(episode.toTVShowEpisode(), EpisodeDownloadPackage(episodeCurrentLocation))
                logger.info { "Stored episode ${episode.toPrettyString()} in library" }

            } catch (e: Throwable) {
                logger.error(e) { "Failed to store episode in library" }
            }
        }
    }
}