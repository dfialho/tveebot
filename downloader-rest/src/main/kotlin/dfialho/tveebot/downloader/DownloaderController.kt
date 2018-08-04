package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to control the [DownloadEngine].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@RestController
class DownloaderController(private val engine: DownloadEngine) {

    /**
     * Adds a new download to the download engine from a [magnetLink] and returns a reference for it.
     *
     * If the download was already being download then this will have no effect.
     */
    @GetMapping("add")
    fun add(magnetLink: String): DownloadReference {
        return engine.add(magnetLink).reference
    }

    /**
     * Retrieves the [DownloadStatus] of all downloads currently managed by the download engine.
     */
    @GetMapping("status")
    fun status(): List<DownloadStatus> {
        return engine.getAllStatus()
    }

    /**
     * Retrieves the current [DownloadStatus] of the download referenced by [reference].
     *
     * @throws NoSuchElementException If not download with [reference] can be found
     */
    @GetMapping("status/{reference}")
    fun status(@PathVariable reference: DownloadReference): DownloadStatus {
        return engine.getHandleOrFail(reference).getStatus()
    }

    /**
     * Removes the download referenced by [reference].
     *
     * @throws NoSuchElementException If not download with [reference] can be found
     */
    @GetMapping("remove/{reference}")
    fun remove(@PathVariable reference: DownloadReference) {
        engine.getHandleOrFail(reference).stop()
    }
}