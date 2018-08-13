package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentStatus
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.api.DownloadState
import dfialho.tveebot.downloader.api.DownloadStatus
import java.util.*


/**
 * [DownloadHandle] implementation based on the libtorrent library.
 *
 * @property engine Internal download engine which creates this handle
 * @property nativeHandle Handle obtained through the libtorrent library
 *
 * @author David Fialho (dfialho@protonmail.com)
 * @see DownloadHandle
 */
class LibTorrentDownloadHandle(
    private val engine: LibTorrentDownloadEngine,
    private val nativeHandle: TorrentHandle
) : DownloadHandle {

    companion object {

        /**
         * Mapping between [TorrentStatus.State] libtorrent states and download states
         */
        private val stateMapper = EnumMap<TorrentStatus.State, DownloadState>(mapOf(
            TorrentStatus.State.CHECKING_FILES to DownloadState.SCANNING_FILES,
            TorrentStatus.State.DOWNLOADING_METADATA to DownloadState.DOWNLOADING_METADATA,
            TorrentStatus.State.DOWNLOADING to DownloadState.DOWNLOADING,
            TorrentStatus.State.FINISHED to DownloadState.FINISHED,
            TorrentStatus.State.SEEDING to DownloadState.FINISHED,
            TorrentStatus.State.ALLOCATING to DownloadState.UNKNOWN,
            TorrentStatus.State.CHECKING_RESUME_DATA to DownloadState.UNKNOWN,
            TorrentStatus.State.UNKNOWN to DownloadState.UNKNOWN
        ))

        /**
         * Converts a [TorrentStatus.State] to a [DownloadState].
         */
        fun TorrentStatus.State.toDownloadState(): DownloadState {
            return checkNotNull(stateMapper[this]) { "Unrecognized state ${this}" }
        }
    }

    override val reference: DownloadReference
        get() = nativeHandle.infoHash().toDownloadReference()

    override val isValid: Boolean
        get() = nativeHandle.isValid

    override fun getStatus(): DownloadStatus = nativeHandle.status().let {
        DownloadStatus(
            nativeHandle.name(),
            reference.value,
            it.state().toDownloadState(),
            it.progress(),
            it.downloadRate()
        )
    }

    override fun stop() {
        engine.remove(reference, nativeHandle)
    }

    override fun pause() {
        nativeHandle.pause()
    }

    override fun resume() {
        nativeHandle.resume()
    }
}