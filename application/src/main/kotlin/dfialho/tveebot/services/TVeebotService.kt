package dfialho.tveebot.services

import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.exceptions.NotFoundException
import dfialho.tveebot.services.models.NewEpisodeParameters
import dfialho.tveebot.toEpisodeFile
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.toTVShow
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.TVShowID
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tvShowEpisodeFileOf
import mu.KLogging

class TVeebotService(
    private val downloader: DownloaderService,
    private val repository: TrackerRepository,
    private val alertService: AlertService
) : Service {
    companion object : KLogging()

    override val name: String
        get() = "TVeebot Service"

    override fun start() = logStart(logger) {
        alertService.subscribe(Alerts.StartedTrackingTVShow, this) { onStartedTrackingTVShow(it) }
        alertService.subscribe(Alerts.StoppedTrackingTVShow, this) { onStoppedTrackingTVShow(it) }
        alertService.subscribe(Alerts.NewEpisodeFound, this) { onNewEpisodeFound(it) }
        alertService.subscribe(Alerts.DownloadFinished, this) { onFinishedDownload(it) }
    }

    override fun stop() = logStop(logger) {
        alertService.unsubscribe(Alerts.StartedTrackingTVShow, this)
        alertService.unsubscribe(Alerts.StoppedTrackingTVShow, this)
        alertService.unsubscribe(Alerts.NewEpisodeFound, this)
        alertService.unsubscribe(Alerts.DownloadFinished, this)
    }

    private fun onStartedTrackingTVShow(tvShow: TVShow) {
        downloadEpisodesFrom(tvShow)
    }

    private fun onStoppedTrackingTVShow(tvShow: TVShow) {
        downloader.removeAllFrom(tvShow.id)
    }

    private fun onNewEpisodeFound(parameters: NewEpisodeParameters) {
        downloadEpisode(parameters.episode, parameters.tvShowVideoQuality)
    }

    private fun onFinishedDownload(reference: DownloadReference) {
        val download = repository.findDownload(reference) ?: throw IllegalStateException("Download was already removed")

        logger.info { "Finished downloading: ${download.episode.toPrettyString()}" }
        repository.removeDownload(download.reference)
    }

    /**
     * Sets the video quality of the TV show identified by [tvShowID] to [newQuality].
     *
     * If [newQuality] corresponds to the same quality of the specified TV show, then this
     * method has not effect.
     *
     * @throws NotFoundException if no TV show is found with the specified [tvShowID].
     */
    fun setTVShowVideoQuality(tvShowID: TVShowID, newQuality: VideoQuality) {
        val originalTVShow = repository.findTrackedTVShow(tvShowID)
            ?: throw NotFoundException("TV Show with ID '$tvShowID' not found")

        repository.setTVShowVideoQuality(tvShowID, newQuality)

        if (originalTVShow.quality != newQuality) {
            with(originalTVShow) {
                logger.info { "Changed video quality of '$title' from $quality to $newQuality" }
            }

            removeDownloadsFrom(tvShowID)
            downloadEpisodesFrom(originalTVShow.copy(quality = newQuality).toTVShow())

        } else {
            logger.info { "Video quality of '${originalTVShow.title}' was not changed" }
        }
    }

    private fun downloadEpisode(episode: TVShowEpisodeFile, tvShowVideoQuality: VideoQuality) {
        // Enforce that only episode files of a specified video quality are downloaded
        if (episode.quality == tvShowVideoQuality) {
            downloader.download(episode)
            logger.info { "Started downloading: ${episode.toPrettyString()}" }
        }
    }

    private fun downloadEpisodesFrom(tvShow: TVShow) {
        for (episode in repository.findEpisodesFrom(tvShow.id)) {
            downloadEpisode(
                episode = tvShowEpisodeFileOf(tvShow, episode.toEpisodeFile()),
                tvShowVideoQuality = tvShow.quality
            )
        }
    }

    private fun removeDownloadsFrom(tvShowID: TVShowID) {
        repository.removeAllDownloadsFrom(tvShowID)
        repository.findDownloadsFrom(tvShowID).forEach { download ->
            downloader.remove(download.reference)
            logger.info { "Stopped downloading: ${download.episode.toPrettyString()}" }
        }
    }
}
