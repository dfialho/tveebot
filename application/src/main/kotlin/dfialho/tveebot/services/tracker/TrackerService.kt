package dfialho.tveebot.services.tracker

import dfialho.tveebot.services.downloader.DownloaderService
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackedTVShow
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.api.TrackingListener
import dfialho.tveebot.tracker.api.VideoQuality
import mu.KLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.util.*
import kotlin.NoSuchElementException

@Service
class TrackerService(
    private val engine: TrackerEngine,
    private val provider: TVShowProvider,
    private val repository: TrackerRepository,
    private val downloaderService: DownloaderService
) : TrackingListener, InitializingBean, DisposableBean {

    companion object : KLogging()

    override fun afterPropertiesSet() {
        logger.debug { "Starting tracker service" }

        repository.putAll(provider.fetchTVShows())
        logger.trace { "Repository: ${repository.findAllTVShows()}" }

        engine.start()
        engine.addListener(this)

        logger.info { "Started tracker service successfully" }
    }

    override fun destroy() {
        logger.debug { "Stopping tracker service" }
        engine.stop()
        engine.removeListener(this)
        logger.info { "Stopped tracker service successfully" }
    }

    override fun notify(tvShow: TVShow, episodeFile: EpisodeFile) {
        episodeFile.episode.apply {
            logger.info { "New episode from ${tvShow.title}: ${season}x%02d - $title".format(number) }
        }

        downloaderService.download(tvShow.id, episodeFile)
    }

    /**
     * Returns a list containing every TV show currently being tracked.
     */
    fun getTrackedTVShows(): List<TrackedTVShow> = repository.findTrackedTVShows()

    /**
     * Returns a list containing every TV show currently NOT being tracked.
     */
    fun getNotTrackedTVShows(): List<TVShow> = repository.findNotTrackedTVShows()

    /**
     * Tells this tracker service to start tracking TV show identified by [tvShowUUID]. Downloaded episode files for
     * this TV show must be of the specified [videoQuality].
     */
    fun trackTVShow(tvShowUUID: UUID, videoQuality: VideoQuality) {
        repository.setTracked(tvShowUUID, videoQuality)
    }

    /**
     * Tells this tracker service to stop tracking TV show identified by [tvShowUUID].
     */
    fun untrackTVShow(tvShowUUID: UUID) {
        downloaderService.removeAllFrom(tvShowUUID)
        repository.setNotTracked(tvShowUUID)
    }

    fun setTVShowVideoQuality(tvShowUUID: UUID, videoQuality: VideoQuality) {
        val tvShow = repository.findTrackedTVShow(tvShowUUID)
            ?: throw NoSuchElementException("No TV show with ID '$tvShowUUID' is being tracked")

        // Remove any downloads of episode files of a different quality
        if (tvShow.quality != videoQuality) {
            downloaderService.removeAllFrom(tvShowUUID)
        }

        repository.setTVShowVideoQuality(tvShowUUID, videoQuality)
    }
}
