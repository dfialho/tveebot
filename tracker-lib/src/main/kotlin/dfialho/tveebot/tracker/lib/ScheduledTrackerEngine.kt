package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.AbstractScheduledService
import dfialho.tveebot.tracker.api.*
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.utils.succeeded
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
    private val trackingList: TrackingList,
    private val episodeLedger: EpisodeLedger,
    private val checkPeriod: Long
) : TrackerEngine, AbstractScheduledService() {

    companion object : KLogging()

    /**
     * Set holding every [TrackingListener] to be notified of new episode files.
     */
    private val listeners: MutableSet<TrackingListener> = mutableSetOf()

    // Use a scheduler which calls [runOneIteration] periodically to check for new episodes
    override fun scheduler(): Scheduler = Scheduler.newFixedRateSchedule(1, checkPeriod, TimeUnit.SECONDS)

    override fun runOneIteration() {
       check()
    }

    override fun start() {
        startAsync()
        awaitRunning()
    }

    override fun stop() {
        stopAsync()
        awaitTerminated()
    }

    override fun check() {
        try {
            logger.debug { "Checking for new episodes..." }

            for (tvShow in trackingList) {

                val episodes: List<TVShowEpisodeFile> = try {
                    provider.fetchEpisodes(tvShow)
                } catch (e: IOException) {
                    logger.error(e) { "Failed to fetch episodes for '${tvShow.title}' from the provider" }
                    continue
                }

                logger.trace { "Episodes available from TV Show '${tvShow.title}': $episodes" }

                for (episode in episodes) {
                    if (episodeLedger.appendOrUpdate(episode).succeeded) {
                        logger.debug { "New episode: $episode" }
                        listeners.forEach { it.onNewEpisode(episode, tvShow.quality) }
                    }
                }
            }

        } catch (e: Exception) {
            logger.error(e) { "Unexpected error occurred while fetching events from provider" }
        }
    }

    override fun addListener(listener: TrackingListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TrackingListener) {
        listeners.remove(listener)
    }
}
