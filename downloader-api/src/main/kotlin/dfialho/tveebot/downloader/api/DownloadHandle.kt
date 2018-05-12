package dfialho.tveebot.downloader.api

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

    /**
     * Pauses the download associated with this handle. The download can be resumed with the [resume] method. If the
     * download is not paused this method has no effect.
     *
     * @see resume
     */
    fun pause()

    /**
     * Resumes the download associated with this handle after it has been paused. If the download is not paused this
     * method has no effect.
     *
     * @see pause
     */
    fun resume()

}
