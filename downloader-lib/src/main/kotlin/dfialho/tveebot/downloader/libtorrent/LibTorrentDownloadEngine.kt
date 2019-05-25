package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.AddTorrentParams.parseMagnetUri
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert
import com.google.common.util.concurrent.AbstractIdleService
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadListener
import dfialho.tveebot.downloader.api.DownloadReference
import java.nio.file.Path
import javax.annotation.concurrent.NotThreadSafe

/**
 * Implementation of a [DownloadEngine] based on the libtorrent library. This implementation is not thread-safe.
 *
 * @param savePath The path to the directory where the downloads are saved.
 * @author David Fialho (dfialho@protonmail.com)
 */
@NotThreadSafe
class LibTorrentDownloadEngine(private val savePath: Path) : AbstractIdleService(), DownloadEngine {

    /**
     * Internal session which manages the downloads.
     */
    private val session = SessionManager()

    /**
     * Set containing the references for every download currently managed by this download engine.
     */
    private val references: MutableSet<DownloadReference> = mutableSetOf()

    /**
     * Set containing the [DownloadListener]s to be notified of changes to the downloads manager by this engine.
     */
    private val listeners: MutableSet<DownloadListener> = mutableSetOf()

    init {
        // Register a native listener to have [listeners] be notified of changes to the downloads' state
        session.addListener(NativeListener())
    }

    override fun startUp() {
        start()
    }

    override fun shutDown() {
        stop()
    }

    override fun start() {
        session.start()
    }

    override fun stop() {
        session.stop()
    }

    private fun requireRunning() {
        if (!session.isRunning) {
            throw IllegalStateException("cannot perform this action while the download engine is not running")
        }
    }

    override fun add(torrentFile: Path): DownloadHandle {
        requireRunning()

        val torrentInfo = TorrentInfo(torrentFile.toFile())
        session.download(torrentInfo, savePath.toFile())
        return resumeDownload(torrentInfo.infoHash())
    }

    override fun add(magnetLink: String): DownloadHandle {
        requireRunning()
        require(magnetLink.startsWith("magnet")) { "the magnet link's scheme must be 'magnet'" }

        session.download(magnetLink, savePath.toFile())
        return resumeDownload(parseMagnetUri(magnetLink).infoHash())
    }

    override fun remove(reference: DownloadReference): Boolean {
        requireRunning()

        getNativeHandle(reference)?.let {
            remove(reference, it)
            return true
        }

        return false
    }

    override fun getHandle(reference: DownloadReference): DownloadHandle? {
        requireRunning()

        return getNativeHandle(reference)?.let {
            LibTorrentDownloadHandle(engine = this, nativeHandle = it)
        }
    }

    override fun getAllHandles(): List<DownloadHandle> {
        requireRunning()
        return references.mapNotNull { getHandle(it) }
    }

    override fun addListener(listener: DownloadListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: DownloadListener) {
        listeners.remove(listener)
    }

    /**
     * Takes the reference and the native handle for a download and removes it from this engine.
     */
    internal fun remove(reference: DownloadReference, nativeHandle: TorrentHandle) {
        requireRunning()

        session.remove(nativeHandle)
        references.remove(reference)
    }

    /**
     * Returns the native handle for the download referenced by [reference]. If no download with reference exists, then
     * it returns null.
     */
    private fun getNativeHandle(reference: DownloadReference): TorrentHandle? {
        return session.find(reference.toHash())
    }

    /**
     * Resumes a download from its [infoHash].
     */
    private fun resumeDownload(infoHash: Sha1Hash): DownloadHandle {
        val torrentHandle: TorrentHandle = session.find(infoHash)
        torrentHandle.resume()

        val handle = LibTorrentDownloadHandle(this, torrentHandle)
        references.add(handle.reference)

        return handle
    }

    /**
     * An implementation of the native [AlertListener] used to notify [DownloadListener]s registered with the
     * [DownloadEngine] of changes to the downloads.
     */
    private inner class NativeListener : AlertListener {

        override fun types(): IntArray? {
            // Listen for alerts indicating a download has finished
            return intArrayOf(AlertType.TORRENT_FINISHED.swig())
        }

        override fun alert(alert: Alert<*>?) {

            // Monitoring only finished downloads
            if (alert is TorrentFinishedAlert) {
                // Notify every listener registered with the engine
                listeners.forEach { it.onFinishedDownload(LibTorrentDownloadHandle(this@LibTorrentDownloadEngine, alert.handle())) }
            }
        }
    }
}
