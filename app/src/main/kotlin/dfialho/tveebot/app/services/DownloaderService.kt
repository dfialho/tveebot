package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.components.CleanupResult
import dfialho.tveebot.app.components.DownloadCleaner
import dfialho.tveebot.app.components.DownloadTracker
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.events.unsubscribe
import dfialho.tveebot.downloader.api.*
import mu.KLogging

class DownloaderService(
    private val engine: DownloadEngine,
    private val downloads: DownloadTracker,
    private val cleaner: DownloadCleaner,
    private val eventBus: EventBus
) : Service {

    companion object : KLogging()

    private val engineListener = EngineListener()

    override fun start() {
        logger.debug { "Starting download engine..." }
        engine.start()
        engine.addListener(engineListener)

        restartDownloads()

        subscribe<Event.EpisodeFileFound>(eventBus) {
            download(it.episode)
        }
    }

    override fun stop() {

        unsubscribe<Event.EpisodeFileFound>(eventBus)

        logger.debug { "Stopping download engine..." }
        engine.stop()
        engine.removeListener(engineListener)
    }

    private fun download(episodeFile: EpisodeFile) {

        val download = engine.add(episodeFile.file.link)
        downloads[download.reference] = episodeFile

        logger.info { "Started downloading: ${episodeFile.episodes}" }
        eventBus.fire(Event.DownloadStarted(episodeFile))
    }

    private fun restartDownloads() {

        downloads.list().forEach {
            logger.debug { "Restarting download: $it" }
            download(it)
        }
    }

    private inner class EngineListener : DownloadListener {

        override fun onFinishedDownload(download: Download) {

            val episodeFile = downloads[download.reference]

            if (episodeFile == null) {
                logger.warn { "Finished download corresponds to an episode that is not being tracked: " +
                        "reference=${download.reference} and savePath=${download.savePath}" }
                return
            }

            downloads.remove(download.reference)
            engine.remove(download.reference)
            logger.info { "Finished downloading: ${episodeFile.episodes}" }

            when(val result = cleaner.cleanUp(download.savePath)) {
                is CleanupResult.Success -> eventBus.fire(Event.DownloadFinished(episodeFile, result.path))
                is CleanupResult.VideoFileNotFound -> logger.error { "Couldn't find any video file in: ${download.savePath}" }
                is CleanupResult.PathNotExists -> logger.error { "Save path does not exist: ${download.savePath}" }
                is CleanupResult.UnsupportedFileType -> logger.error { "Unexpected file type at: : ${download.savePath}" }
                is CleanupResult.UnexpectedError -> logger.error(result.exception) {
                    "Unexpected error while cleaning up: : ${download.savePath}"
                }
            }
        }

    }

    fun getStatus(): List<DownloadStatus> {

        return engine.getDownloads()
            .map { it.status }
    }
}
