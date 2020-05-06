package dfialho.tveebot.downloader.api

/**
 * The [DownloadEngine] is responsible for performing the actual downloads, providing an interface to manage them.
 */
interface DownloadEngine {

    /**
     * Starts the engine, blocking until it is ready to be used.
     */
    fun start()

    /**
     * Stops the engine cleanly.
     */
    fun stop()

    /**
     * Adds a download to the engine based on a magnet link and returns a [Download] for the newly added
     * download. The download is immediately resumed once added. This method does not wait for the download to finish.
     *
     * If the download corresponding to the [magnetLink] was already added to the engine, then this method has no
     * effect.
     */
    fun add(magnetLink: String): Download

    /**
     * Removes the download referenced by [reference] from this engine. If [reference] does not map to any download
     * managed by this engine, then this method will have no effect.
     */
    fun remove(reference: DownloadReference)

    /**
     * Retrieves all [Download]s currently being managed by the engine.
     */
    fun getDownloads(): List<Download>

    /**
     * Adds a new [DownloadListener] to be notified when every download finishes.
     */
    fun addListener(listener: DownloadListener)

    /**
     * Removes a [DownloadListener]. After calling this method, [listener] will no longer be notified of download that
     * have finished.
     */
    fun removeListener(listener: DownloadListener)
}