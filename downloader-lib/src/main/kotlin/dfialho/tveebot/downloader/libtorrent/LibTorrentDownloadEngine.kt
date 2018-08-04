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
import javax.annotation.concurrent.NotThreadSafe

/**
 * Implementation of a [DownloadEngine] based on the libtorrent library. This implementation is not thread-safe.
 *
 * @property savePath The path to the directory where the downloads are saved.
 *
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

    override fun remove(reference: DownloadReference) {
        requireRunning()

        getNativeHandle(reference)?.let {
            remove(reference, it)
        }
    }

    override fun removeOrFail(reference: DownloadReference) {
        requireRunning()

        val nativeHandle = getNativeHandle(reference) ?: throwNotFoundError(reference)
        remove(reference, nativeHandle)
    }

    override fun getHandle(reference: DownloadReference): DownloadHandle? {
        requireRunning()

        return getNativeHandle(reference)?.let {
            LibTorrentDownloadHandle(this, it)
        }
    }

    override fun getHandleOrFail(reference: DownloadReference): DownloadHandle {
        return getHandle(reference) ?: throwNotFoundError(reference)
    }

    override fun getAllHandles(): List<DownloadHandle> {
        requireRunning()
        return references.mapNotNull { getHandle(it) }
    }

    override fun getAllStatus(): List<DownloadStatus> {
        requireRunning()
        return getAllHandles().map { it.getStatus() }
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
     * Throws exception indicating the download corresponding to [reference] does not exist in this engine.
     */
    private fun throwNotFoundError(reference: DownloadReference): Nothing {
        throw NoSuchElementException("Download with reference '$reference' not found")
    }
}
