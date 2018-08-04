package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.AddTorrentParams.parseMagnetUri
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType.TORRENT_FINISHED
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert
import dfialho.tveebot.downloader.api.*
import dfialho.tveebot.downloader.api.EventListener
import java.nio.file.Path
import java.util.*
import kotlin.NoSuchElementException

/**
 * Implementation of a [DownloadEngine] based on the libtorrent library.
 *
 * @property savePath The path to the directory where the downloads saved.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class LibTorrentDownloadEngine(private val savePath: Path) : DownloadEngine {

    /**
     * Internal session which manages the downloads.
     */
    private val session = SessionManager()

    private val references: MutableSet<DownloadReference> = mutableSetOf()

    /**
     * Mapping between the event listeners and the internal alert listener. Required to be able to remove an event
     * listener.
     */
    private val listeners: MutableMap<EventListener, AlertListener> = HashMap()

    override fun start() {
        session.start()
    }

    override fun stop() {
        session.stop()
    }

    override fun add(torrentFile: Path): DownloadHandle {
        val torrentInfo = TorrentInfo(torrentFile.toFile())
        session.download(torrentInfo, savePath.toFile())

        return resumeDownload(torrentInfo.infoHash())
    }

    override fun add(magnetLink: String): DownloadHandle {
        require(magnetLink.startsWith("magnet")) { "the magnet link's scheme must be 'magnet'" }

        session.download(magnetLink, savePath.toFile())
        return resumeDownload(parseMagnetUri(magnetLink).infoHash())
    }

    override fun addListener(listener: EventListener) {
        val internalListener = InternalListener(listener)

        if (listeners.putIfAbsent(listener, internalListener) == null) {
            session.addListener(internalListener)
        }
    }

    override fun removeListener(listener: EventListener) {
        listeners.remove(listener)?.also { session.removeListener(it) }
    }

    override fun getHandle(reference: DownloadReference): DownloadHandle? {
        val torrentHandle: TorrentHandle? = session.find(reference.toHash())
        return torrentHandle?.let { LibTorrentDownloadHandle(this, torrentHandle) }
    }

    override fun getHandleOrFail(reference: DownloadReference): DownloadHandle {
        return getHandle(reference) ?: throw NoSuchElementException("Download with reference '$reference' not found")
    }

    override fun getAllHandles(): List<DownloadHandle> {
        return references.mapNotNull { getHandle(it) }
    }

    override fun getAllStatus(): List<DownloadStatus> {
        return getAllHandles().map { it.getStatus() }
    }

    /**
     * Removes a download corresponding to the [torrentHandle] from the download session. The downloaded data will be
     * kept.
     */
    internal fun remove(torrentHandle: TorrentHandle) {
        session.remove(torrentHandle)
    }

    /**
     * Resumes a download from its [infoHash].
     */
    private fun resumeDownload(infoHash: Sha1Hash): DownloadHandle {
        val torrentHandle = session.find(infoHash)
        torrentHandle.resume()

        val handle = LibTorrentDownloadHandle(this, torrentHandle)
        references.add(handle.reference)

        return handle
    }

    /**
     * An internal listener which wraps an [EventListener] with an [AlertListener].
     */
    private class InternalListener(private val listener: EventListener) : AlertListener {

        override fun types(): IntArray? {
            // Listen to all alerts
            return null
        }

        override fun alert(alert: Alert<*>) {

            when (alert.type()) {
                TORRENT_FINISHED -> {
                    alert as TorrentFinishedAlert
                    listener.onDownloadFinished(DownloadReference(alert.handle().infoHash().toHex()))
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }
}
