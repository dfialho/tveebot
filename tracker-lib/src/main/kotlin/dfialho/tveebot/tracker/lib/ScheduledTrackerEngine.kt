package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.AbstractScheduledService
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.EpisodeRecorder
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingListener
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
    override val recorder: EpisodeRecorder,
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

        for (trackedTVShow in recorder.getTVShows()) {

            val episodes: List<EpisodeFile> = try {
                provider.fetchEpisodes(trackedTVShow.toTVShow())
            } catch (e: IOException) {
                logger.warn(e) { "Failed to fetch episodes for '${trackedTVShow.title}' from the provider" }
                continue
            }

            logger.trace { "Episodes for '${trackedTVShow.title}': $episodes" }

            for (episode in episodes) {
                val isNewEpisode: Boolean = recorder.putOrUpdateIfMoreRecent(trackedTVShow, episode)

                if (isNewEpisode) {
                    logger.debug { "New episode: $episode" }
                    listeners.forEach { it.notify(trackedTVShow, episode) }
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
