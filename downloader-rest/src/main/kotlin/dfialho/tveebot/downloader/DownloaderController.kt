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
     */
    @GetMapping("add")
    fun add(magnetLink: String): DownloadReference {
        val downloadHandle = downloadEngine.add(magnetLink)
        return downloadHandle.reference
    }

    /**
     * Retrieves the current [DownloadStatus] of the download referenced by [downloadReference].
     */
    @GetMapping("status")
    fun status(downloadReference: DownloadReference): DownloadStatus {
        val downloadHandle = downloadEngine.getHandle(downloadReference)
        return downloadHandle.getStatus()
    }

}