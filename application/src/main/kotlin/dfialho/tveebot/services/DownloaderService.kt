package dfialho.tveebot.services

import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadListener
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import dfialho.tveebot.data.EpisodeDownload
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import mu.KLogging
import java.util.*

class DownloaderService(
    private val engine: DownloadEngine,
    private val repository: TrackerRepository

) : Service, DownloadListener {

    companion object : KLogging()

    override fun start() {
        logger.debug { "Starting downloader service" }

        engine.start()
        engine.addListener(this)

        // Restart every download in the queue
        val episodeDownloads: List<EpisodeDownload> = repository.findAllDownloads()
        episodeDownloads.forEach { engine.add(it.episode.link) }
        episodeDownloads.forEach { engine.add(it.episode.link) }
        logger.info { "Restarted downloading ${episodeDownloads.size} episodes" }

        logger.info { "Started downloader service successfully" }
    }

    override fun stop() {
        logger.debug { "Stopping downloader service" }
        engine.stop()
        engine.removeListener(this)
        logger.info { "Stopped downloader service successfully" }
    }

    override fun notifyFinished(handle: DownloadHandle) {
        val download = repository.findDownload(handle.reference)
            ?: throw IllegalStateException("Download was already removed")

        with(download) {
            logger.info { "Finished downloading: ${tvShow.title} - ${episode.toPrettyString()}" }
        }

        repository.removeDownload(handle.reference)
    }

    /**
     * Adds a new download to the download engine from a [magnetLink] and returns a reference for it.
     * If the download was already being download then this will have no effect.
     */
    fun download(magnetLink: String): DownloadReference {
        return engine.add(magnetLink).reference
    }

    /**
     * Starts downloading the [episodeFile].
     */
    fun download(tvShow: TVShow, episodeFile: EpisodeFile) {
        val handle = engine.add(episodeFile.link)
        repository.put(EpisodeDownload(handle.reference, tvShow, episodeFile))

        logger.info { "Started downloading: ${tvShow.title} - ${episodeFile.toPrettyString()}" }
    }

    /**
     * Retrieves the [DownloadStatus] of all downloads currently managed by the download engine.
     */
    fun getAllStatus(): List<DownloadStatus> {
        return engine.getAllHandles().map { it.getStatus() }
    }

    /**
     * Retrieves the current [DownloadStatus] of the download referenced by [reference].
     *
     * @throws NoSuchElementException If not download with [reference] can be found
     */
    fun getStatus(reference: DownloadReference): DownloadStatus {
        return engine.getHandle(reference)?.getStatus() ?: throwNotFoundError(reference)
    }

    /**
     * Removes the download referenced by [reference].
     *
     * @throws NoSuchElementException If not download with [reference] can be found
     */
    fun removeDownload(reference: DownloadReference) {

        if (!engine.remove(reference)) {
            throwNotFoundError(reference)
        }

        repository.removeDownload(reference)
    }

    /**
     * Removes every episode download corresponding to the TV show identified by [tvShowUUID].
     */
    fun removeAllFrom(tvShowUUID: UUID) {
        repository.removeAllDownloadsFrom(tvShowUUID)
        repository.findDownloadsFrom(tvShowUUID).forEach {
            engine.remove(it.reference)
            logger.info { "Stopped downloading: ${it.tvShow.title} - ${it.episode.toPrettyString()}" }
        }
    }
}

/**
 * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
 */
private fun throwNotFoundError(reference: DownloadReference): Nothing {
    throw NoSuchElementException("Download with reference '$reference' not found")
}
