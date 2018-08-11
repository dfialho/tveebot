package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.AbstractScheduledService
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.api.isMoreRecentThan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class ScheduledTrackerEngine(
    override val provider: TVShowProvider,
    override val repository: TrackerRepository
) : TrackerEngine, AbstractScheduledService() {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ScheduledTrackerEngine::class.java)
    }

    override fun scheduler(): Scheduler = Scheduler.newFixedRateSchedule(1, 30, TimeUnit.SECONDS)

    override fun runOneIteration() {
        logger.info("Checking for new episodes...")

        try {
            for (tvShow in repository.findTVShows(tracked = true)) {
                val episodeFiles = provider.fetchEpisodes(tvShow)
                logger.trace("Episodes for '${tvShow.title}': $episodeFiles")

                val existingEpisodeFiles = repository.findAllVideosFor(tvShow)
                    .associateBy { it.identifier }

                for (episodeFile in episodeFiles) {
                    val existingFile: EpisodeFile? = existingEpisodeFiles[episodeFile.identifier]

                    if (existingFile == null || episodeFile isMoreRecentThan existingFile) {
                        logger.debug("New episode: $episodeFile")
                        repository.put(tvShow, episodeFile)
                    }
                }
            }

        } catch (e: Exception) {
            logger.warn("Failed to retrieve  service failed", e)
        }
    }

    override fun start() {
        startAsync()
        awaitRunning()
    }

    override fun stop() {
        stopAsync()
        awaitTerminated()
    }
}
