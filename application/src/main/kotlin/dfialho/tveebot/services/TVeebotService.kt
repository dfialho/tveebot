package dfialho.tveebot.services

import dfialho.tveebot.application.api.EpisodeState
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.episodeFileOf
import dfialho.tveebot.services.models.DownloadNotification
import dfialho.tveebot.services.models.NewEpisodeNotification
import dfialho.tveebot.services.models.StoreNotification
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.toTVShow
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.VideoQuality
import mu.KLogging

class TVeebotService(
    private val tracker: TrackerService,
    private val downloader: DownloaderService,
    private val organizer: OrganizerService,
    private val repository: TrackerRepository,
    private val alertService: AlertService
) : Service {
    companion object : KLogging()

    override val name: String = TVeebotService::class.simpleName!!

    override fun start() = logStart(logger) {
        alertService.subscribe(Alerts.StartedTrackingTVShow, this) { onStartedTrackingTVShow(it) }
        alertService.subscribe(Alerts.StoppedTrackingTVShow, this) { onStoppedTrackingTVShow(it) }
        alertService.subscribe(Alerts.NewEpisodeFound, this) { onNewEpisodeFound(it) }
        alertService.subscribe(Alerts.DownloadStarted, this) { onStartedDownload(it) }
        alertService.subscribe(Alerts.DownloadStopped, this) { onStoppedDownload(it) }
        alertService.subscribe(Alerts.DownloadFinished, this) { onFinishedDownload(it) }
        alertService.subscribe(Alerts.EpisodeStored, this) { onEpisodeStored(it) }

        organizer.start()
        downloader.start()

        logger.debug { "Restarting downloads of episodes being downloaded before the last shutdown" }
        repository.findTrackedTVShows().forEach {
            downloadEpisodesFrom(it.toTVShow())
        }

        tracker.start()
    }

    override fun stop() = logStop(logger) {
        tracker.stop()
        downloader.stop()
        organizer.stop()

        alertService.unsubscribe(Alerts.StartedTrackingTVShow, this)
        alertService.unsubscribe(Alerts.StoppedTrackingTVShow, this)
        alertService.unsubscribe(Alerts.NewEpisodeFound, this)
        alertService.unsubscribe(Alerts.DownloadFinished, this)
        alertService.unsubscribe(Alerts.DownloadStarted, this)
        alertService.unsubscribe(Alerts.DownloadStopped, this)
        alertService.unsubscribe(Alerts.EpisodeStored, this)
    }

    private fun onStartedTrackingTVShow(tvShow: TVShow) {
        logger.debug { "Start downloading episodes already available for '${tvShow.toPrettyString()}'" }
        downloadEpisodesFrom(tvShow)

        logger.debug { "Triggered episode check after starting to track TV show '${tvShow.toPrettyString()}'" }
        tracker.check()
    }

    private fun onStoppedTrackingTVShow(tvShow: TVShow) {
        removeDownloadsFrom(tvShow.id)
        logger.info { "Stopped downloading episodes from TV Show: ${tvShow.toPrettyString()}" }
    }

    private fun onNewEpisodeFound(notification: NewEpisodeNotification): Unit = with(notification) {
        val tvShow = repository.findTrackedTVShow(episodeFile.episode.tvShow.id)

        if (tvShow == null) {
            logger.info { "Skipping episode file '${episodeFile.toPrettyString()}' because respective TV Show is not being tracked" }
            return
        }

        downloadEpisode(episodeFile, tvShow.quality)
    }

    private fun onStartedDownload(notification: DownloadNotification) {
        logger.info { "Started downloading: ${notification.episodeFile.toPrettyString()}" }
        repository.setEpisodeState(notification.episodeFile, EpisodeState.DOWNLOADING)
    }

    private fun onStoppedDownload(notification: DownloadNotification) {
        logger.info { "Stopped downloading: ${notification.episodeFile.toPrettyString()}" }
        repository.setEpisodeState(notification.episodeFile, EpisodeState.AVAILABLE)
    }

    private fun onFinishedDownload(notification: DownloadNotification): Unit = with(notification) {
        logger.info { "Finished downloading episode: ${notification.episodeFile.toPrettyString()}" }
        organizer.store(episodeFile, savePath)
    }

    private fun onEpisodeStored(notification: StoreNotification): Unit = with(notification) {
        logger.info { "Stored episode '${episodeFile.toPrettyString()}' in '${storePath.toAbsolutePath()}'" }
        repository.setEpisodeState(episodeFile, EpisodeState.STORED)
    }

    private fun downloadEpisodesFrom(tvShow: TVShow) {

        repository.findTrackedTVShow(tvShow.id)?.let { trackedTVShow ->
            repository.findEpisodesFrom(trackedTVShow.id).asSequence()
                .filter { it.state == EpisodeState.AVAILABLE || it.state == EpisodeState.DOWNLOADING }
                .filter { it.quality == trackedTVShow.quality }
                .map { episodeFileOf(trackedTVShow, it) }
                .forEach { downloadEpisode(it, trackedTVShow.quality) }
        }
    }

    private fun downloadEpisode(episodeFile: EpisodeFile, tvShowQuality: VideoQuality) {

        // Enforce that only episodeFile files of a specified video quality are downloaded
        if (episodeFile.quality == tvShowQuality) {
            downloader.download(episodeFile)
        }
    }

    private fun removeDownloadsFrom(tvShowID: ID) {
        downloader.removeByTVShow(tvShowID)
    }
}
