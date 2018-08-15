package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadReference

/**
 * An [EpisodeDownloadQueue] holds the downloads currently being downloaded by a [DownloadEngine].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface EpisodeDownloadQueue {

    /**
     * Pushes [episodeDownload] into the queue. Should be called when a new download is added to the [DownloadEngine].
     */
    fun push(episodeDownload: EpisodeDownload)

    /**
     * Removes the [EpisodeDownload] with the given download [reference]. If the queue does not contain a download
     * with [reference], then this methods has no effect.
     */
    fun remove(reference: DownloadReference)

    /**
     * Returns a list containing every [EpisodeDownload] in the queue.
     */
    fun getAll(): List<EpisodeDownload>
}
