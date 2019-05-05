package dfialho.tveebot.services

import dfialho.tveebot.application.api.EpisodeDownload
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.*
import dfialho.tveebot.exceptions.NotFoundException
import dfialho.tveebot.services.models.FinishedDownloadParameters
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import mu.KLogging
import java.util.*

class DownloaderService(
    private val engine: DownloadEngine,
    private val repository: TrackerRepository,
    private val alertService: AlertService

) : Service, DownloadListener {

    companion object : KLogging()

    override val name: String
        get() = "Downloader Service"

    override fun start() = logStart(logger) {
        engine.start()
        engine.addListener(this)

        // Restart every download in the queue
        val episodeDownloads: List<EpisodeDownload> = repository.findAllDownloads()
        episodeDownloads.forEach { engine.add(it.episode.link) }
        logger.info { "Restarted downloading ${episodeDownloads.size} episodes" }
    }

    override fun stop() = logStop(logger) {
        engine.stop()
        engine.removeListener(this)
    }

    override fun notifyFinished(handle: DownloadHandle) {
        logger.debug { "Finished downloading: ${handle.reference}" }

        // Extract information from handle before removing download
        // Afterwards the handle becomes invalid
        val reference = handle.reference
        val savePath = handle.savePath
        engine.remove(handle.reference)

        alertService.raiseAlert(Alerts.DownloadFinished, FinishedDownloadParameters(reference, savePath))
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

        if (!engine.remove(reference)) {
            throwNotFoundError(reference)
        }

        repository.removeDownload(reference)
    }

    /**
     * Removes all downloads in the given references.
     */
    fun removeAll(references: List<DownloadReference>) {
        references.forEach { engine.remove(it) }
        repository.removeAllDownloads(references)
    }
}

/**
 * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
 */
private fun throwNotFoundError(reference: DownloadReference): Nothing {
    throw NotFoundException("Download with reference '$reference' not found")
}
