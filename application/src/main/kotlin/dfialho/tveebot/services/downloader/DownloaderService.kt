package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadListener
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import dfialho.tveebot.services.tracker.TrackerService
import dfialho.tveebot.tracker.api.EpisodeFile
import mu.KLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service responsible for downloading the episode files found by the [TrackerService].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@Service
class DownloaderService(
    private val engine: DownloadEngine,
    private val downloadQueue: EpisodeDownloadQueue
) : DownloadListener, InitializingBean, DisposableBean {

    companion object : KLogging()

    override fun afterPropertiesSet() {
        logger.debug { "Starting downloader service" }

        engine.start()
        engine.addListener(this)

        // Restart every download in the queue
        val episodeDownloads: List<EpisodeDownload> = downloadQueue.getAll()
        episodeDownloads.forEach { engine.add(it.episodeFile.link) }
        logger.info { "Restarted downloading ${episodeDownloads.size} episodes" }

        logger.info { "Started downloader service successfully" }
    }

    override fun destroy() {
        logger.debug { "Stopping downloader service" }
        engine.stop()
        engine.removeListener(this)
        logger.info { "Stopped downloader service successfully" }
    }

    override fun notifyFinished(handle: DownloadHandle) {
        logger.info { "Finished downloading: ${handle.getStatus().name}" }
        downloadQueue.remove(handle.reference)
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
    fun download(tvShowUUID: UUID, episodeFile: EpisodeFile) {
        val handle = engine.add(episodeFile.link)
        downloadQueue.push(EpisodeDownload(handle.reference, tvShowUUID, episodeFile))
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

        downloadQueue.remove(reference)
    }

    /**
     * Removes every episode download corresponding to the TV show identified by [tvShowUUID].
     */
    fun removeAllFrom(tvShowUUID: UUID) {

        val downloads: List<EpisodeDownload> = downloadQueue.getAll()
            .filter { it.tvShowUUID == tvShowUUID }

        downloads.forEach {
            engine.remove(it.reference)
            downloadQueue.remove(it.reference)
        }
    }
}

/**
 * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
 */
private fun throwNotFoundError(reference: DownloadReference): Nothing {
    throw NoSuchElementException("Download with reference '$reference' not found")
}
