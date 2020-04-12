package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.events.unsubscribe
import dfialho.tveebot.app.services.downloader.DownloadTracker
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadListener
import mu.KLogging

class DownloaderService(
    private val engine: DownloadEngine,
    private val downloads: DownloadTracker,
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

        val handle = engine.add(episodeFile.file.link)
        downloads[handle.reference] = episodeFile

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

        override fun onFinishedDownload(handle: DownloadHandle) {

            val episodeFile = downloads[handle.reference]

            if (episodeFile == null) {
                logger.warn { "Received an handle for an download that was not being tracked: " +
                        "reference=${handle.reference} and savePath=${handle.savePath}" }
                return
            }

            downloads.remove(handle.reference)
            logger.info { "Finished downloading: ${episodeFile.episodes}" }
            eventBus.fire(Event.DownloadFinished(episodeFile, handle.savePath))

            engine.remove(handle.reference)
        }
    }
}
