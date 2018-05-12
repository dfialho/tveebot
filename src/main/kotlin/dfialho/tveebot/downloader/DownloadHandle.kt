package dfialho.tveebot.downloader

/**
 * Handle for a single download. It is used to perform actions for the download and to retrieve information about it.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface DownloadHandle {

    /**
     * Reference to the download corresponding to this handle.
     */
    val reference: DownloadReference

    /**
     * Indicates whether or not the handle is still valid. It is true if this handle refers to a valid download, or
     * false if otherwise. An handle becomes invalid when stopped.
     */
    val isValid: Boolean

    /**
     * Obtains the status of the download. Depending on the implementation, this operation might be slow. With that in
     * mind avoid calling it multiple times to access each of its individual properties.
     */
    fun getStatus(): DownloadStatus

    /**
     * Stops the download associated with this handle. Afterwords this handle becomes invalid.
     */
    fun stop()

}
