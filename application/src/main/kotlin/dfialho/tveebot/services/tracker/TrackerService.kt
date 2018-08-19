package dfialho.tveebot.services.tracker

import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.data.TrackerRepositoryException
import dfialho.tveebot.services.downloader.DownloaderService
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingListener
import dfialho.tveebot.tracker.api.VideoQuality
import mu.KLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.util.*
import kotlin.NoSuchElementException

/**
 * Service responsible for tracking TV shows and send episode files to the [DownloaderService] once a new episode is
 * found for a tracked TV show.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
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

    // Invoked when the tracker engine finds a new episode
    override fun notify(tvShow: TVShow, episode: EpisodeFile) {
        logger.info { "New episode: ${tvShow.title} - ${episode.toPrettyString()}" }
        downloadEpisode(tvShow, episode)
    }

    /**
     * Returns a list with every TV show in the repository.
     */
    fun getAllTVShows(): List<TVShow> = repository.findAllTVShows()

    /**
     * Returns a list containing every TV show currently being tracked.
     */
    fun getTrackedTVShows(): List<TVShow> = repository.findTrackedTVShows()

    /**
     * Returns a list containing every TV show currently NOT being tracked.
     */
    fun getNotTrackedTVShows(): List<TVShow> = repository.findNotTrackedTVShows()

    /**
     * Returns a map associating each TV show to its episodes.
     */
    fun getAllEpisodesByTVShow(): Map<UUID, List<EpisodeFile>> = repository.findEpisodesByTVShow().mapKeys { it.key.id }

    /**
     * Returns a list containing every episode from the TV show identified by [tvShowUUID]
     */
    fun getEpisodesFrom(tvShowUUID: UUID): List<EpisodeFile> = repository.findEpisodesFrom(tvShowUUID)

    /**
     * Tells this tracker service to start tracking TV show identified by [tvShowUUID]. Downloaded episode files for
     * this TV show must be of the specified [videoQuality].
     */
    fun trackTVShow(tvShowUUID: UUID, videoQuality: VideoQuality) {
        repository.setTracked(tvShowUUID, videoQuality)

        repository.findTrackedTVShow(tvShowUUID)?.let {
            downloadEpisodesFrom(it)
            logger.info { "Started tracking TV show: ${it.title}" }
        }
    }

    /**
     * Tells this tracker service to stop tracking TV show identified by [tvShowUUID].
     */
    fun untrackTVShow(tvShowUUID: UUID) {
        repository.setNotTracked(tvShowUUID)
        downloaderService.removeAllFrom(tvShowUUID)
        repository.removeEpisodesFrom(tvShowUUID)

        logger.info { "Stopped tracking TV show: ${repository.findTrackedTVShow(tvShowUUID)?.title}" }
    }

    /**
     * Sets the [newVideoQuality] of episode files corresponding to the TV show identified by [tvShowUUID].
     */
    fun setTVShowVideoQuality(tvShowUUID: UUID, newVideoQuality: VideoQuality) {

        val originalTVShow = repository.findTrackedTVShow(tvShowUUID)

        try {
            repository.setTVShowVideoQuality(tvShowUUID, newVideoQuality)
        } catch (e: TrackerRepositoryException) {
            throw NoSuchElementException("No TV show with ID '$tvShowUUID' is being tracked")
        }

        if (originalTVShow != null && originalTVShow.quality != newVideoQuality) {
            logger.info { "Changed video quality of '${originalTVShow.title}' from ${originalTVShow.quality} to $newVideoQuality" }

            // Remove any downloads of episode files of a different quality
            downloaderService.removeAllFrom(tvShowUUID)

            // Start downloading every episode already found with the new video quality
            val tvShowWithNewQuality = originalTVShow.copy(quality = newVideoQuality)
            downloadEpisodesFrom(tvShowWithNewQuality)
        } else {
            logger.info { "Video quality of '${originalTVShow?.title}' not changed" }
        }
    }

    private fun downloadEpisode(tvShow: TVShow, episode: EpisodeFile) {
        // Enforce that only episode files of a specified video quality are downloaded
        if (episode.quality == tvShow.quality) {
            downloaderService.download(tvShow, episode)
        }
    }

    private fun downloadEpisodesFrom(tvShow: TVShow) {
        repository.findEpisodesFrom(tvShow.id).forEach { downloadEpisode(tvShow, it) }
    }
}
