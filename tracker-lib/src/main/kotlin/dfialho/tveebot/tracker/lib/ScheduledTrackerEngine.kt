package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.AbstractScheduledService
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.api.TrackingListener
import dfialho.tveebot.tracker.api.isMoreRecentThan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * An implementation of [TrackerEngine] which periodically checks for new episodes.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ScheduledTrackerEngine(
    override val provider: TVShowProvider,
    override val repository: TrackerRepository
) : TrackerEngine, AbstractScheduledService() {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ScheduledTrackerEngine::class.java)
    }

    /**
     * Set holding every [TrackingListener] to be notified of new episode files.
     */
    private val listeners: MutableSet<TrackingListener> = mutableSetOf()

    // Use a scheduler which call [runOneIteration] periodically to check for new episodes
    override fun scheduler(): Scheduler = Scheduler.newFixedRateSchedule(1, 5, TimeUnit.SECONDS)

    override fun runOneIteration() {
        logger.info("Checking for new episodes...")

        try {
            for (tvShow in repository.findTVShows(tracked = true)) {
                val episodeFiles = provider.fetchEpisodes(tvShow)
                logger.trace("Episodes for '${tvShow.title}': $episodeFiles")

                val existingEpisodeFiles = repository.findEpisodeFilesFrom(tvShow)
                    .associateBy { it.identifier }

                for (episodeFile in episodeFiles) {
                    val existingFile: EpisodeFile? = existingEpisodeFiles[episodeFile.identifier]

                    if (existingFile == null || episodeFile isMoreRecentThan existingFile) {
                        logger.info("New episode: $episodeFile")
                        repository.put(tvShow, episodeFile)
                        listeners.forEach { it.notify(tvShow, episodeFile) }
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

    override fun addListener(listener: TrackingListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TrackingListener) {
        listeners.remove(listener)
    }
}
