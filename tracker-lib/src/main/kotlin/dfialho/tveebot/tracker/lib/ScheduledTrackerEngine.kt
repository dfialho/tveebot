package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.AbstractScheduledService
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShow
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingListener
import dfialho.tveebot.utils.succeeded
import mu.KLogging
import java.io.IOException
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * An implementation of [TrackerEngine] which periodically checks for new episodes.
 */
class ScheduledTrackerEngine(
    override val provider: TVShowProvider,
    private val episodeLedger: EpisodeLedger,
    private val checkPeriod: Duration
) : TrackerEngine, AbstractScheduledService() {

    companion object : KLogging()

    private val trackingList = ConcurrentHashMap<String, TVShow>()

    /**
     * Set holding every [TrackingListener] to be notified of new episode files.
     */
    private val listeners: MutableSet<TrackingListener> = mutableSetOf()

    // Use a scheduler which calls [runOneIteration] periodically to check for new episodes
    override fun scheduler(): Scheduler = Scheduler.newFixedRateSchedule(1, checkPeriod.toMillis(), TimeUnit.MILLISECONDS)

    override fun runOneIteration() {
        try {
            check()
        } catch (e: Throwable) {
            logger.error(e) { "Unexpected error while checking for new episodes" }
        }
    }

    override fun start() {
        logger.debug { "Starting tracker engine..." }
        startAsync()
        awaitRunning()
        logger.debug { "Started tracker engine" }
    }

    override fun stop() {
        logger.debug { "Stopping tracker engine..." }
        stopAsync()
        awaitTerminated()
        logger.debug { "Stopped tracker engine" }
    }

    override fun register(tvShow: TVShow) {
        trackingList[tvShow.id] = tvShow
    }

    override fun unregister(tvShowId: String) {
        trackingList.remove(tvShowId)
    }

    override fun check() {
        logger.info { "Checking for new episodes..." }

        for (tvShow in trackingList.values) {

            val episodes: List<EpisodeFile> = try {
                provider.fetchEpisodes(tvShow)
            } catch (e: IOException) {
                logger.error(e) { "Failed to fetch episodes for '${tvShow.title}' from the provider" }
                continue
            }

            logger.debug { "Episodes available from TV Show '${tvShow.title}': $episodes" }

            for (episode in episodes) {
                if (episodeLedger.appendOrUpdate(episode).succeeded) {
                    logger.debug { "New episode file: $episode" }
                    listeners.forEach { it.onNewEpisode(episode) }
                } else {
                    logger.debug { "Episode file ignored: $episode" }
                }
            }
        }
    }

    override fun addListener(listener: TrackingListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TrackingListener) {
        listeners.remove(listener)
    }
}
