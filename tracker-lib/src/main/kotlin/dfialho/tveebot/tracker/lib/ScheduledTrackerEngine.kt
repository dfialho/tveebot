package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.AbstractScheduledService
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.api.TrackingListener
import dfialho.tveebot.tracker.api.isMoreRecentThan
import mu.KLogging
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * An implementation of [TrackerEngine] which periodically checks for new episodes.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ScheduledTrackerEngine(
    override val provider: TVShowProvider,
    override val repository: TrackerRepository,
    private val checkPeriod: Long
) : TrackerEngine, AbstractScheduledService() {

    companion object : KLogging()

    /**
     * Set holding every [TrackingListener] to be notified of new episode files.
     */
    private val listeners: MutableSet<TrackingListener> = mutableSetOf()

    // Use a scheduler which call [runOneIteration] periodically to check for new episodes
    override fun scheduler(): Scheduler = Scheduler.newFixedRateSchedule(1, checkPeriod, TimeUnit.SECONDS)

    override fun runOneIteration(): Unit = try {
        logger.info { "Checking for new episodes..." }

        for (tvShow in repository.findTVShows(tracked = true)) {

            val episodeFiles: List<EpisodeFile> = try {
                provider.fetchEpisodes(tvShow)
            } catch (e: IOException) {
                logger.warn(e) { "Failed to fetch episodes for '${tvShow.title}' from the provider" }
                continue
            }

            logger.trace { "Episodes for '${tvShow.title}': $episodeFiles" }

            val existingEpisodeFiles = repository.findEpisodeFilesFrom(tvShow)
                .associateBy { it.identifier }

            for (episodeFile in episodeFiles) {
                val existingFile: EpisodeFile? = existingEpisodeFiles[episodeFile.identifier]

                if (existingFile == null || episodeFile isMoreRecentThan existingFile) {
                    logger.debug { "New episode: $episodeFile" }
                    listeners.forEach { it.notify(tvShow, episodeFile) }
                    repository.put(tvShow, episodeFile)
                }
            }
        }

    } catch (e: Exception) {
        logger.error(e) { "Unexpected error occurred while fetching events from provider" }
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
