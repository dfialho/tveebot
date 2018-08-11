package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

@Service
class DownloaderService(val engine: DownloadEngine) : InitializingBean, DisposableBean {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DownloaderService::class.java)
    }

    override fun afterPropertiesSet() {
        logger.debug("Starting downloader service")
        engine.start()
        logger.info("Started downloader service successfully")
    }

    override fun destroy() {
        logger.debug("Stopping downloader service")
        engine.stop()
        logger.info("Stopped downloader service successfully")
    }

    /**
     * Adds a new download to the download engine from a [magnetLink] and returns a reference for it.
     *
     * If the download was already being download then this will have no effect.
     */
    fun download(magnetLink: String): DownloadReference {
        return engine.add(magnetLink).reference
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
    }

    /**
     * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
     */
    private fun throwNotFoundError(reference: DownloadReference): Nothing {
        throw NoSuchElementException("Download with reference '$reference' not found")
    }
}
