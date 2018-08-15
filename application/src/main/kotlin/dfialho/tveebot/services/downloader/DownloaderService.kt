package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadListener
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import dfialho.tveebot.downloader.libtorrent.MagnetLink
import dfialho.tveebot.tracker.api.TVShow
import mu.KLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.util.*

@Service
class DownloaderService(
    private val engine: DownloadEngine,
    private val downloadQueue: DownloadQueue
) : DownloadListener, InitializingBean, DisposableBean {

    companion object : KLogging()

    override fun afterPropertiesSet() {
        logger.debug { "Starting downloader service" }

        engine.start()
        engine.addListener(this)

        // Restart every download in the queue
        val downloadLinks = downloadQueue.getLinks()
        downloadLinks.forEach { it.download(engine) }
        logger.info { "Restarted ${downloadLinks.size} downloads" }

        logger.info { "Started downloader service successfully" }
    }

    override fun destroy() {
        logger.debug { "Stopping downloader service" }
        engine.stop()
        engine.removeListener(this)
        logger.info { "Stopped downloader service successfully" }
    }

    override fun notifyFinished(handle: DownloadHandle) {
        downloadQueue.remove(handle.reference)
        logger.info { "Finished downloading: ${handle.getStatus().name}" }
    }

    /**
     * Adds a new download to the download engine from a [magnetLink] and returns a reference for it.
     * If the download was already being download then this will have no effect.
     */
    fun download(magnetLink: String): DownloadReference {
        return engine.add(magnetLink).reference.also {
            downloadQueue.push(it, MagnetLink(magnetLink))
        }
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

    fun removeTVShowDownloads(tvShow: TVShow) {

    }

    fun removeTVShowDownloads(uuid: UUID) {

    }

    /**
     * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
     */
    private fun throwNotFoundError(reference: DownloadReference): Nothing {
        throw NoSuchElementException("Download with reference '$reference' not found")
    }
}
