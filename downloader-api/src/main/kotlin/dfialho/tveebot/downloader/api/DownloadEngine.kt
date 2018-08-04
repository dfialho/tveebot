package dfialho.tveebot.downloader.api

import java.nio.file.Path
import javax.annotation.concurrent.NotThreadSafe

/**
 * The [DownloadEngine] is responsible for performing the actual downloads, providing an interface to manage them.
 *
 * @author David Fialho (dfialho@protonmail.com)
 * @see DownloadManager
 */
@NotThreadSafe
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
     * Adds a download to the engine based on a torrent file and returns a [DownloadHandle] for the newly added
     * download. The download is immediately resumed once added. This method does not wait for the download to finish.
     *
     * If the download corresponding to the [torrentFile] was already added to the engine, then this method has no
     * effect.
     */
    fun add(torrentFile: Path): DownloadHandle

    /**
     * Adds a download to the engine based on a magnet link and returns a [DownloadHandle] for the newly added
     * download. The download is immediately resumed once added. This method does not wait for the download to finish.
     *
     * If the download corresponding to the [magnetLink] was already added to the engine, then this method has no
     * effect.
     */
    fun add(magnetLink: String): DownloadHandle

    /**
     * Removes the download referenced by [reference] from this engine. If [reference] does not map to any download
     * managed by this engine, then this method will have no effect.
     *
     * After calling this method, all [DownloadHandle]s for the corresponding download become invalid.
     */
    fun remove(reference: DownloadReference)

    /**
     * Performs the same action as [remove], but it throws a [NoSuchElementException] if no download is found with the
     * specified [reference].
     *
     * @throws NoSuchElementException If no download with [reference] can be found
     */
    fun removeOrFail(reference: DownloadReference)

    /**
     * Returns the [DownloadHandle] for the download referenced by [reference]. If no download with [reference] exists,
     * then it returns null.
     */
    fun getHandle(reference: DownloadReference): DownloadHandle?

    /**
     * Returns the [DownloadHandle] for the download referenced by [reference], if it exists. Otherwise, it throws a
     * [NoSuchElementException].
     *
     * @throws NoSuchElementException If no download with [reference] can be found
     */
    fun getHandleOrFail(reference: DownloadReference): DownloadHandle

    /**
     * Retrieves the [DownloadHandle] for all downloads being managed by this engine.
     */
    fun getAllHandles(): List<DownloadHandle>

    /**
     * Retrieves the [DownloadStatus] for all downloads being managed by this engine.
     */
    fun getAllStatus(): List<DownloadStatus>
}