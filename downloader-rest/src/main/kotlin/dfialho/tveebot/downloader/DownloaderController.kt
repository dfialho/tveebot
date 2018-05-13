package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DownloaderController(private val downloadEngine: DownloadEngine) {

    @GetMapping("add")
    fun add(magnetLink: String): DownloadReference {
        val downloadHandle = downloadEngine.add(magnetLink)
        return downloadHandle.reference
    }

    @GetMapping("status")
    fun status(downloadReference: DownloadReference): DownloadStatus {
        val downloadHandle = downloadEngine.getHandle(downloadReference)
        return downloadHandle.getStatus()
    }

}