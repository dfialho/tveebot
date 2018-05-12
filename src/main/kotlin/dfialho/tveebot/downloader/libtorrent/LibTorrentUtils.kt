package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.Sha1Hash
import dfialho.tveebot.downloader.DownloadReference

/**
 * Converts this [Sha1Hash] to a [DownloadReference].
 */
internal fun Sha1Hash.toDownloadReference(): DownloadReference = DownloadReference(this.toHex())
