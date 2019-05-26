package dfialho.tveebot.services

import dfialho.tveebot.downloader.api.*
import dfialho.tveebot.exceptions.NotFoundException
import dfialho.tveebot.repositories.DownloadPool
import dfialho.tveebot.services.models.FinishedDownloadNotification
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.ID
import mu.KLogging
import java.util.*

class DownloaderService(
    private val engine: DownloadEngine,
    private val downloadPool: DownloadPool,
    private val alertService: AlertService

) : Service {

    companion object : KLogging()

    override val name: String = DownloaderService::class.simpleName!!

    private inner class EngineListener : DownloadListener {
        override fun onFinishedDownload(handle: DownloadHandle) {
            logger.debug { "Finished downloading: ${handle.reference}" }

            // Extract information from handle before removing download
            // Afterwards the handle becomes invalid
            val reference = handle.reference
            val savePath = handle.savePath
            engine.remove(handle.reference)
            val episodeFile = downloadPool.remove(reference)
                ?: throw IllegalStateException("Cannot find download with reference: $reference")

            alertService.raiseAlert(Alerts.DownloadFinished, FinishedDownloadNotification(episodeFile, savePath))
        }
    }

    private val engineListener = EngineListener()

    override fun start() = logStart(logger) {
        engine.start()
        engine.addListener(engineListener)

        logger.debug { "Restarting downloads of episodes being downloaded before the last shutdown" }
        downloadPool.listUnstarted().forEach { download(it) }
    }

    override fun stop() = logStop(logger) {
        engine.stop()
        engine.removeListener(engineListener)
    }

    /**
     * Starts downloading the [episodeFile].
     */
    fun download(episodeFile: EpisodeFile) {
        val handle = engine.add(episodeFile.link)
        logger.debug { "Started downloading episode: ${episodeFile.toPrettyString()}" }

        downloadPool.put(handle.reference, episodeFile)
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
    fun remove(reference: DownloadReference) {
        val handle = engine.getHandle(reference) ?: throwNotFoundError(reference)
        downloadPool.remove(reference)

        logger.debug { "Cleanup temporary data from download: $reference" }
        handle.savePath.toFile().deleteRecursively()

        logger.debug { "Removing download from engine: $reference" }
        engine.remove(reference)
    }

    /**
     * Removes all downloads in the given references.
     */
    fun removeByTVShow(tvShowID: ID) {
        downloadPool.listByTVShow(tvShowID).forEach { (reference, episodeFile) ->
            remove(reference)
            logger.debug { "Stopped downloading episode: ${episodeFile.toPrettyString()}" }
        }
    }
}

/**
 * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
 */
private fun throwNotFoundError(reference: DownloadReference): Nothing {
    throw NotFoundException("Download with reference '$reference' not found")
}
