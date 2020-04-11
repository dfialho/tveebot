package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.MoreExecutors
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
import java.util.concurrent.*

/**
 * An implementation of [TrackerEngine] which periodically checks for new episodes.
 */
class ScheduledTrackerEngine(
    override val provider: TVShowProvider,
    private val episodeLedger: EpisodeLedger,
    private val checkPeriod: Duration
) : TrackerEngine {

    companion object : KLogging()

    private val trackingList: MutableMap<String, TVShow> = ConcurrentHashMap()
    private val listeners: MutableSet<TrackingListener> = CopyOnWriteArraySet()
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    override fun start() {
        logger.debug { "Started tracker engine" }
        executor.scheduleCheck()
    }

    override fun stop() {
        logger.debug { "Stopping tracker engine..." }
        MoreExecutors.shutdownAndAwaitTermination(executor, 30, TimeUnit.SECONDS)
        logger.debug { "Stopped tracker engine" }
    }

    override fun register(tvShow: TVShow) {
        trackingList[tvShow.id] = tvShow
    }

    override fun unregister(tvShowId: String) {
        trackingList.remove(tvShowId)
    }

    override fun check() {
        executor.submit {
            checkNow()
        }
    }

    private fun ScheduledExecutorService.scheduleCheck() {
        schedule(Callable {
            checkNow()
            scheduleCheck()
        }, checkPeriod.toMillis(), TimeUnit.MILLISECONDS)
    }

    private fun checkNow() {

        try {
            logger.info { "Checking for new episodes..." }

            for (tvShow in trackingList.values) {

                val episodes: List<EpisodeFile> = try {
                    provider.fetchEpisodes(tvShow)
                } catch (e: IOException) {
                    logger.error(e) { "Failed to fetch episodes for '${tvShow.title}' from the provider" }
                    continue
                }

                logger.trace { "Episodes available from TV Show '${tvShow.title}': $episodes" }

                for (episode in episodes) {
                    if (episodeLedger.appendOrUpdate(episode).succeeded) {
                        logger.debug { "New episode file: $episode" }
                        listeners.forEach { it.onNewEpisode(episode) }
                    } else {
                        logger.trace { "Episode file ignored: $episode" }
                    }
                }
            }

            logger.debug { "Finished checking" }

        } catch (e: Throwable) {
            logger.error(e) { "Unexpected error while checking for new episodes" }
        }
    }

    override fun addListener(listener: TrackingListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TrackingListener) {
        listeners.remove(listener)
    }
}
