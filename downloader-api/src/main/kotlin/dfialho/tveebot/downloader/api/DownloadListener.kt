package dfialho.tveebot.downloader.api

/**
 * A [DownloadListener] is notified by the [DownloadEngine] with which it is registered about relevant events regarding
 * the downloads, such as, when a download has finished.
 */
interface DownloadListener {

    /**
     * Invoked with the [download] that has finished downloading.
     */
    fun onFinishedDownload(download: Download)
}
