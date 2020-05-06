package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.AddTorrentParams.parseMagnetUri
import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.Sha1Hash
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert
import dfialho.tveebot.downloader.api.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.annotation.concurrent.NotThreadSafe

/**
 * Implementation of a [DownloadEngine] based on the libtorrent library. This implementation is not thread-safe.
 *
 * @param savePath The path to the directory where the downloads are saved.
 * @author David Fialho (dfialho@protonmail.com)
 */
@NotThreadSafe
class LibTorrentDownloadEngine(private val savePath: Path) : DownloadEngine {

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

    override fun add(magnetLink: String): Download {
        requireRunning()
        require(magnetLink.startsWith("magnet")) { "the magnet link's scheme must be 'magnet'" }

        session.download(magnetLink, savePath.toFile())
        return resumeDownload(parseMagnetUri(magnetLink).infoHash())
    }

    override fun getDownloads(): List<Download> {
        requireRunning()
        return references
            .mapNotNull { getNativeHandle(it) }
            .map { it.toDownload() }
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
    private fun remove(reference: DownloadReference, handle: TorrentHandle) {
        requireRunning()

        session.remove(handle)
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
    private fun resumeDownload(infoHash: Sha1Hash): Download {
        val handle: TorrentHandle = session.find(infoHash)
        handle.resume()

        val download = handle.toDownload()
        references.add(download.reference)
        return download
    }

    private fun TorrentHandle.toDownload(): Download {

        val reference = infoHash().toDownloadReference()
        return Download(
            reference,
            name = name(),
            savePath = Paths.get(savePath(), name()),
            status = status().let {
                DownloadStatus(
                    it.state().toDownloadState(),
                    it.progress(),
                    it.downloadRate()
                )
            }
        )
    }

    /**
     * An implementation of the native [AlertListener] used to notify [DownloadListener]s registered with the
     * [DownloadEngine] of changes to the downloads.
     */
    private inner class NativeListener : AlertListener {

        private val executor = Executors.newSingleThreadExecutor()

        override fun types(): IntArray? {
            // Listen for alerts indicating a download has finished
            return intArrayOf(AlertType.TORRENT_FINISHED.swig())
        }

        override fun alert(alert: Alert<*>?) {

            if (alert is TorrentFinishedAlert) {
                val handle = alert.handle()
                val download = handle.toDownload()
                remove(download.reference, handle)

                for (listener in listeners) {
                    executor.submit { listener.onFinishedDownload(download) }
                }
            }
        }
    }
}
