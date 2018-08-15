package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.Sha1Hash
import dfialho.tveebot.downloader.api.DownloadReference

/**
 * Converts this [Sha1Hash] to a [DownloadReference].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
internal fun Sha1Hash.toDownloadReference(): DownloadReference = DownloadReference(this.toHex())

/**
 * Converts this [DownloadReference] to a [Sha1Hash].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
internal fun DownloadReference.toHash(): Sha1Hash = Sha1Hash(this.value)