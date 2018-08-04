package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.Sha1Hash
import dfialho.tveebot.downloader.api.DownloadReference

/**
 * Converts this [Sha1Hash] to a [DownloadReference].
 */
internal fun Sha1Hash.toDownloadReference(): DownloadReference = DownloadReference(this.toHex())

/**
 * Converts this [DownloadReference] to a [Sha1Hash].
 */
internal fun DownloadReference.toHash(): Sha1Hash = Sha1Hash(this.reference)