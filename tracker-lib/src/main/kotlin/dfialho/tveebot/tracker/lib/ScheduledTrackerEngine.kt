package dfialho.tveebot.tracker.lib

import com.google.common.util.concurrent.AbstractScheduledService
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class ScheduledTrackerEngine(
    override val provider: TVShowProvider,
    private val repository: TrackerRepository
) : TrackerEngine, AbstractScheduledService() {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ScheduledTrackerEngine::class.java)
    }

    override fun scheduler(): Scheduler = Scheduler.newFixedRateSchedule(0, 30, TimeUnit.SECONDS)

    override fun runOneIteration() {
        logger.info("Checking for new episodes...")

        try {
            for (tvShow in repository.findAllTVShows()) {
                val episodes = provider.fetchEpisodes(tvShow)

                logger.info("Episodes for '${tvShow.title}': $episodes")
                // select only new episodes
                // move new episodes to downloader
            }

        } catch (e: Exception) {
            logger.warn("Tracker service failed", e)
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

    override fun add(tvShow: TVShow) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(tvShow: TVShow) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeTVShow(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
