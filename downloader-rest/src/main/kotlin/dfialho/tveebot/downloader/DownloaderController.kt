package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to control the [DownloadEngine].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@RestController
class DownloaderController(private val downloadEngine: DownloadEngine) {

    /**
     * Adds a new download to the download engine from a [magnetLink] and returns a reference for it.
     *
     * If the download was already being download then this will have no effect.
     */
    @GetMapping("add")
    fun add(magnetLink: String): DownloadReference {
        return downloadEngine.add(magnetLink).reference
    }

    /**
     * Retrieves the current [DownloadStatus] of the download referenced by [downloadReference].
     *
     * @throws NoSuchElementException If not download with [downloadReference] can be found
     */
    @GetMapping("status")
    fun status(downloadReference: DownloadReference): DownloadStatus {
        return downloadEngine.getHandle(downloadReference).getStatus()
    }

    /**
     * Removes the download referenced by [downloadReference].
     *
     * @throws NoSuchElementException If not download with [downloadReference] can be found
     */
    @GetMapping("remove")
    fun remove(downloadReference: DownloadReference) {
        downloadEngine.getHandle(downloadReference).stop()
    }
}