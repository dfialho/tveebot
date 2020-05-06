package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.TorrentStatus
import dfialho.tveebot.downloader.api.DownloadState
import java.util.*

/**
 * Mapping between [TorrentStatus.State] libtorrent states and download states
 */
private val stateMapper = EnumMap<TorrentStatus.State, DownloadState>(
    mapOf(
        TorrentStatus.State.CHECKING_FILES to DownloadState.SCANNING_FILES,
        TorrentStatus.State.DOWNLOADING_METADATA to DownloadState.DOWNLOADING_METADATA,
        TorrentStatus.State.DOWNLOADING to DownloadState.DOWNLOADING,
        TorrentStatus.State.FINISHED to DownloadState.FINISHED,
        TorrentStatus.State.SEEDING to DownloadState.FINISHED,
        TorrentStatus.State.ALLOCATING to DownloadState.UNKNOWN,
        TorrentStatus.State.CHECKING_RESUME_DATA to DownloadState.UNKNOWN,
        TorrentStatus.State.UNKNOWN to DownloadState.UNKNOWN
    )
)

/**
 * Converts a [TorrentStatus.State] to a [DownloadState].
 */
internal fun TorrentStatus.State.toDownloadState(): DownloadState {
    return checkNotNull(stateMapper[this]) { "Unrecognized state $this" }
}

