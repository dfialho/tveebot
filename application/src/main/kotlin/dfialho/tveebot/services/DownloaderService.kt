package dfialho.tveebot.services

import dfialho.tveebot.application.api.EpisodeDownload
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.*
import dfialho.tveebot.exceptions.NotFoundException
import dfialho.tveebot.services.models.FinishedDownloadNotification
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import mu.KLogging
import java.util.*

class DownloaderService(
    private val engine: DownloadEngine,
    private val repository: TrackerRepository,
    private val alertService: AlertService

) : Service {

    companion object : KLogging()

    private inner class EngineListener : DownloadListener {
        override fun onFinishedDownload(handle: DownloadHandle) {
            logger.debug { "Finished downloading: ${handle.reference}" }

            // Extract information from handle before removing download
            // Afterwards the handle becomes invalid
            val reference = handle.reference
            val savePath = handle.savePath
            engine.remove(handle.reference)

            logger.debug { "Fetching download from repository: $reference" }
            val download =
                repository.findDownload(reference) ?: throw IllegalStateException("Cannot find download: $reference")
            repository.removeDownload(reference)
            logger.info { "Finished downloading episode: ${download.episode}" }

            val notification = FinishedDownloadNotification(download.episode, savePath)
            alertService.raiseAlert(Alerts.DownloadFinished, notification)
            logger.debug { "Notification sent to alert service: $notification" }
        }
    }

    private val engineListener = EngineListener()

    override val name: String = DownloaderService::class.simpleName!!

    override fun start() = logStart(logger) {
        engine.start()
        engine.addListener(engineListener)

        // Restart every download in the queue
        val episodeDownloads: List<EpisodeDownload> = repository.findAllDownloads()
        episodeDownloads.forEach { engine.add(it.episode.link) }
        logger.info { "Restarted downloading ${episodeDownloads.size} episodes" }
    }

    override fun stop() = logStop(logger) {
        engine.stop()
        engine.removeListener(engineListener)
    }

    /**
     * Starts downloading the [episodeFile].
     */
    fun download(episodeFile: TVShowEpisodeFile) {
        val handle = engine.add(episodeFile.link)
        repository.put(EpisodeDownload(handle.reference, episodeFile))

        logger.debug { "Downloading episode: ${episodeFile.toPrettyString()} with reference ${handle.reference}" }
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
        repository.removeDownload(reference)
        removeDownload(reference)
    }

    /**
     * Removes all downloads in the given references.
     */
    fun removeAll(references: List<DownloadReference>) {
        repository.removeAllDownloads(references)
        references.forEach { removeDownload(it) }
    }

    private fun removeDownload(reference: DownloadReference) {
        val handle = engine.getHandle(reference) ?: throwNotFoundError(reference)

        logger.debug { "Cleanup temporary data from download: $reference" }
        handle.savePath.toFile().deleteRecursively()

        logger.debug { "Removing download from engine: $reference" }
        engine.remove(reference)
    }
}

/**
 * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
 */
private fun throwNotFoundError(reference: DownloadReference): Nothing {
    throw NotFoundException("Download with reference '$reference' not found")
}
