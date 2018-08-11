package dfialho.tveebot.tracker

import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

@Service
class TrackerService(
    private val engine: TrackerEngine,
    private val provider: TVShowProvider,
    private val repository: TrackerRepository
) : InitializingBean, DisposableBean {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TrackerService::class.java)
    }

    override fun afterPropertiesSet() {
        logger.debug("Starting tracker service")

        repository.putAll(provider.fetchTVShows())
        logger.info("Repository: ${repository.findAllTVShows()}")
        engine.start()

        logger.info("Started tracker service successfully")
    }

    override fun destroy() {
        logger.debug("Stopping tracker service")
        engine.stop()
        logger.info("Stopped tracker service successfully")
    }

    fun getTVShows(tracked: Boolean): List<TVShow> = repository.findTVShows(tracked)

    fun getTrackedTVShows(): List<TVShow> = repository.findTVShows(tracked = true)

    fun getNotTrackedTVShows(): List<TVShow> = repository.findTVShows(tracked = false)
}
