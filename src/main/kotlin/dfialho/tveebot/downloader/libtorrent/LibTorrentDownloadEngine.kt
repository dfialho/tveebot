package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.AddTorrentParams.parseMagnetUri
import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.Sha1Hash
import com.frostwire.jlibtorrent.TorrentInfo
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType.TORRENT_FINISHED
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert
import dfialho.tveebot.downloader.DownloadEngine
import dfialho.tveebot.downloader.DownloadHandle
import dfialho.tveebot.downloader.DownloadReference
import dfialho.tveebot.downloader.EventListener
import java.nio.file.Path

/**
 * Implementation of a [DownloadEngine] based on the libtorrent library.
 *
 * @property savePath The path to the directory where the downloads saved.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class LibTorrentDownloadEngine(private val savePath: Path) : DownloadEngine {

    /**
     * The internal session which manages the downloads.
     */
    private val session = SessionManager()

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
        require(magnetLink.startsWith("magnet")) { "The magnet's scheme must be 'magnet'" }

        session.download(magnetLink, savePath.toFile())
        return resumeDownload(parseMagnetUri(magnetLink).infoHash())
    }

    private fun resumeDownload(infoHash: Sha1Hash): DownloadHandle {
        val torrentHandle = session.find(infoHash)
        torrentHandle.resume()

        return LibTorrentDownloadHandle(torrentHandle)
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
