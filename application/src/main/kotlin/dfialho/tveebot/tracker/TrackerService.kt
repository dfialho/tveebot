package dfialho.tveebot.tracker

import dfialho.tveebot.downloader.DownloaderService
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.api.TrackingListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrackerService(
    private val engine: TrackerEngine,
    private val provider: TVShowProvider,
    private val repository: TrackerRepository,
    private val downloaderService: DownloaderService
) : TrackingListener, InitializingBean, DisposableBean {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TrackerService::class.java)
    }

    override fun afterPropertiesSet() {
        logger.debug("Starting tracker service")

        repository.putAll(provider.fetchTVShows())
        logger.info("Repository: ${repository.findAllTVShows()}")

        engine.start()
        engine.addListener(this)

        logger.info("Started tracker service successfully")
    }

    override fun destroy() {
        logger.debug("Stopping tracker service")
        engine.stop()
        engine.removeListener(this)
        logger.info("Stopped tracker service successfully")
    }

    override fun notify(tvShow: TVShow, episodeFile: EpisodeFile) {
        logger.info("New episode from ${tvShow.title}: $episodeFile")
        downloaderService.download(episodeFile.link)
    }

    /**
     * Returns a list containing every TV show either being [tracked] or not [tracked].
     */
    fun getTVShows(tracked: Boolean): List<TVShow> = repository.findTVShows(tracked)

    /**
     * Sets the TV show identified by [tvShowUUID] as [tracked].
     */
    fun setTVShowTracked(tvShowUUID: UUID, tracked: Boolean) {
        repository.setTracked(tvShowUUID, tracked)
    }

}
