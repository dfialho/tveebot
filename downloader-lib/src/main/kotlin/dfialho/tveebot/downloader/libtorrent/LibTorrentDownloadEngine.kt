package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.AddTorrentParams.parseMagnetUri
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.Sha1Hash
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentInfo
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadStatus
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
     * Internal session which manages the downloads.
     */
    private val session = SessionManager()

    /**
     * Set containing the references for every download currently managed by this download engine.
     */
    private val references: MutableSet<DownloadReference> = mutableSetOf()

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
}
