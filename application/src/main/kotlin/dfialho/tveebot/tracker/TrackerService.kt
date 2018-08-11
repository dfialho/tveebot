package dfialho.tveebot.tracker

import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.lib.InMemoryTVShowIDMapper
import dfialho.tveebot.tracker.lib.InMemoryTrackerRepository
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

@Service
class TrackerService(config: TrackerConfig) : InitializingBean, DisposableBean {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TrackerService::class.java)
    }

    private val tvShow = TVShow("Castle")

    private val engine: TrackerEngine = ScheduledTrackerEngine(
        provider = ShowRSSProvider(idMapper = InMemoryTVShowIDMapper().apply { this[tvShow.id] = "48" }),
        repository = InMemoryTrackerRepository().apply { put(tvShow) }
    )

    override fun afterPropertiesSet() {
        logger.debug("Starting tracker service")
        engine.start()
        logger.info("Started tracker service successfully")
    }

    override fun destroy() {
        logger.debug("Stopping tracker service")
        engine.stop()
        logger.info("Stopped tracker service successfully")
    }
}