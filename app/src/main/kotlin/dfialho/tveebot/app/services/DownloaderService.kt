package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadListener
import dfialho.tveebot.downloader.api.DownloadReference
import mu.KLogging

class DownloaderService(
    private val engine: DownloadEngine,
    private val eventBus: EventBus
) : Service {

    companion object : KLogging()

    private val downloads = mutableMapOf<DownloadReference, EpisodeFile>()
    private val engineListener = EngineListener()

    override fun start() {
        logger.debug { "Starting download engine..." }
        engine.start()
        engine.addListener(engineListener)

        subscribe<Event.EpisodeFileFound>(eventBus) {
            download(it.episode)
        }
    }

    override fun stop() {
        logger.debug { "Stopping download engine..." }
        engine.stop()
        engine.removeListener(engineListener)
    }

    fun download(episode: EpisodeFile) {
        val handle = engine.add(episode.file.link)
        downloads[handle.reference] = episode

        logger.info { "Started downloading: ${episode.episodes}" }
        eventBus.fire(Event.DownloadStarted(episode))
    }

    private inner class EngineListener : DownloadListener {

        override fun onFinishedDownload(handle: DownloadHandle) {

            val episode = downloads[handle.reference]

            if (episode == null) {
                logger.warn { "Received an handle for an download that was not being tracked: " +
                        "reference=${handle.reference} and savePath=${handle.savePath}" }
                return
            }

            logger.debug { "Finished downloading: ${episode.episodes}" }
            engine.remove(handle.reference)
            downloads.remove(handle.reference)
            eventBus.fire(Event.DownloadFinished(episode, handle.savePath))
        }
    }
}
