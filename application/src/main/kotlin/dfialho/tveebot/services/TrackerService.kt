package dfialho.tveebot.services

import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingListener
import dfialho.tveebot.tracker.api.VideoQuality
import mu.KLogging
import java.util.*

class TrackerService(
    private val engine: TrackerEngine,
    private val provider: TVShowProvider,
    private val repository: TrackerRepository,
    private val downloader: DownloaderService

) : Service, TrackingListener {

    companion object : KLogging()

    override fun start() {
        logger.debug { "Starting tracker service" }

        repository.putAll(provider.fetchTVShows())
        logger.trace { "Repository: ${repository.findAllTVShows()}" }

        engine.start()
        engine.addListener(this)

        logger.info { "Started tracker service successfully" }
    }

    override fun stop() {
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
     * Returns a map associating each TV show to its episodes. TV shows without any episode are not included in the
     * returned map.
     */
    fun getAllEpisodesByTVShow(): Map<UUID, List<EpisodeFile>> = repository.findEpisodesByTVShow().mapKeys { it.key.id }

    /**
     * Returns a list containing every episode from the TV show identified by [tvShowUUID].
     *
     * @throws NoSuchElementException if no TV show is found with id [tvShowUUID].
     */
    fun getEpisodesFrom(tvShowUUID: UUID): List<EpisodeFile> = repository.findEpisodesFrom(tvShowUUID)

    /**
     * Tells this tracker service to start tracking TV show identified by [tvShowUUID]. Downloaded episode files for
     * this TV show must be of the specified [videoQuality].
     *
     * @throws IllegalStateException if the TV show with ID [tvShowUUID] is already being tracked.
     * @throws NoSuchElementException if no TV show is found with id [tvShowUUID].
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
     *
     * @throws IllegalStateException if the TV show with ID [tvShowUUID] is already being tracked.
     * @throws NoSuchElementException if no TV show is found with id [tvShowUUID].
     */
    fun untrackTVShow(tvShowUUID: UUID) {
        repository.setNotTracked(tvShowUUID)
        downloader.removeAllFrom(tvShowUUID)

        logger.info { "Stopped tracking TV show: ${repository.findTrackedTVShow(tvShowUUID)?.title}" }
    }

    /**
     * Sets the [newVideoQuality] of episode files corresponding to the TV show identified by [tvShowUUID].
     *
     * @throws NoSuchElementException if no TV show is found with id [tvShowUUID].
     */
    fun setTVShowVideoQuality(tvShowUUID: UUID, newVideoQuality: VideoQuality) {
        val originalTVShow = repository.findTrackedTVShow(tvShowUUID)

        repository.setTVShowVideoQuality(tvShowUUID, newVideoQuality)

        if (originalTVShow != null && originalTVShow.quality != newVideoQuality) {
            logger.info { "Changed video quality of '${originalTVShow.title}' from ${originalTVShow.quality} to $newVideoQuality" }

            // Remove any downloads of episode files of a different quality
            downloader.removeAllFrom(tvShowUUID)

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
            downloader.download(tvShow, episode)
        }
    }

    private fun downloadEpisodesFrom(tvShow: TVShow) {
        repository.findEpisodesFrom(tvShow.id).forEach { downloadEpisode(tvShow, it) }
    }
}