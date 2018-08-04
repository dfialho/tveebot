package dfialho.tveebot.downloader.api

import java.nio.file.Path
import javax.annotation.concurrent.NotThreadSafe

/**
 * The [DownloadEngine] is responsible for performing the actual downloads, providing an interface to manage them.
 *
 * At this point this interface does not "force" the implementors to guarantee thread safeness.
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
     * Adds an event listener to the engine. The listener will be notified of all events while the engine is running
     * and the listener is not removed from the engine.
     *
     * @see EventListener for details on the kind of events that can be triggered.
     */
    fun addListener(listener: EventListener)

    /**
     * Removes an event listener from the engine. After this method returns the [listener] will no longer receive
     * events from the engine.
     *
     * If the listener was not listening for events yet, then this method has no effect.
     */
    fun removeListener(listener: EventListener)

    /**
     * Returns the [DownloadHandle] for the download referenced by [downloadReference].
     */
    fun getHandle(downloadReference: DownloadReference): DownloadHandle

}