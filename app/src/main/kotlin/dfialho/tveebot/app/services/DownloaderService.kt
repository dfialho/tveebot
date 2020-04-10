package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeEntity
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadListener
import dfialho.tveebot.downloader.api.DownloadReference
import mu.KLogging

class DownloaderService(
    private val engine: DownloadEngine,
    private val repository: TVeebotRepository,
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
            onNewEpisodeFile(it.episode)
        }
    }

    override fun stop() {
        logger.debug { "Stopping download engine..." }
        engine.stop()
        engine.removeListener(engineListener)
    }

    fun download(episodeFile: EpisodeFile) {

        repository.transaction {
            episodeFile.episodes
                .map { EpisodeEntity(it, State.DOWNLOADING) }
                .forEach { update(it) }
        }

        val handle = engine.add(episodeFile.file.link)
        downloads[handle.reference] = episodeFile

        logger.info { "Started downloading: ${episodeFile.episodes}" }
        eventBus.fire(Event.DownloadStarted(episodeFile))
    }

    private fun onNewEpisodeFile(episodeFile: EpisodeFile) {

        // TODO Data Model: It should not be possible to have multiple tv shows in an episode file
        val tvShow = episodeFile.episodes[0].tvShow
        val trackedTVShow = repository.findTVShow(tvShow.id, tracked = true)
        if (trackedTVShow == null) {
            logger.info { "Ignoring episode file because the tv show is not tracked: $episodeFile" }
            return
        }

        if (episodeFile.file.quality == trackedTVShow.videoQuality) {
            download(episodeFile)
        } else {
            logger.debug { "Ignoring episode file because the video quality does not match: $episodeFile" }
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

            logger.info { "Finished downloading: ${episodeFile.episodes}" }
            repository.transaction {
                episodeFile.episodes
                    .map { EpisodeEntity(it, State.DOWNLOADED) }
                    .forEach { update(it) }
            }

            engine.remove(handle.reference)
            downloads.remove(handle.reference)
            eventBus.fire(Event.DownloadFinished(episodeFile, handle.savePath))
        }
    }
}
