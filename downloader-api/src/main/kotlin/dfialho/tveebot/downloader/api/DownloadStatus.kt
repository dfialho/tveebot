package dfialho.tveebot.downloader.api

/**
 * Data class to hold information about a download.
 */
data class DownloadStatus(

    /**
     * The current state of the download.
     */
    val state: DownloadState,

    /**
     * A value between 0 and 1 indicating the progress of the download.
     */
    val progress: Float,

    /**
     * The download rate in bytes per second.
     */
    val rate: Int
)
