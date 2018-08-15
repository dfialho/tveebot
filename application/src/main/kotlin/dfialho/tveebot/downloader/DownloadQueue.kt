package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadLink
import dfialho.tveebot.downloader.api.DownloadReference


/**
 * A [DownloadQueue] holds the downloads currently being downloaded by a [DownloadEngine].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface DownloadQueue {

    /**
     * Pushes a new download into the queue. Should be called when a new download is added to the [DownloadEngine].
     */
    fun push(reference: DownloadReference, link: DownloadLink)

    /**
     * Removes the download with the given [reference]. If the queue does not contain a download with [reference], then
     * this methods has no effect.
     */
    fun remove(reference: DownloadReference)

    /**
     * Retrieves the links of every download in the queue.
     */
    fun getLinks(): List<DownloadLink>
}
