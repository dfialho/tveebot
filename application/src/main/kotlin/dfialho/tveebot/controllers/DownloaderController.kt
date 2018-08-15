package dfialho.tveebot.controllers

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import dfialho.tveebot.services.downloader.DownloaderService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Rest controller for the [DownloaderService].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@RestController
@RequestMapping("/downloader")
class DownloaderController(private val downloaderService: DownloaderService) {

    /**
     * Adds a new download to the download engine from a [magnetLink] and returns a reference for it.
     *
     * If the download was already being download then this will have no effect.
     */
    @PutMapping("download")
    fun download(magnetLink: String): DownloadReference {
        return downloaderService.download(magnetLink)
    }

    /**
     * Retrieves the [DownloadStatus] of all downloads currently managed by the download engine.
     */
    @GetMapping("status")
    fun status(): List<DownloadStatus> {
        return downloaderService.getAllStatus()
    }

    /**
     * Retrieves the current [DownloadStatus] of the download referenced by [reference].
     *
     * @throws NoSuchElementException If not download with [reference] can be found
     */
    @GetMapping("status/{reference}")
    fun status(@PathVariable reference: DownloadReference): DownloadStatus {
        return downloaderService.getStatus(reference)
    }

    /**
     * Removes the download referenced by [reference].
     *
     * @throws NoSuchElementException If not download with [reference] can be found
     */
    @DeleteMapping("remove/{reference}")
    fun remove(@PathVariable reference: DownloadReference) {
        downloaderService.removeDownload(reference)
    }
}