package dfialho.tveebot.downloader.libtorrent

import com.frostwire.jlibtorrent.Sha1Hash
import dfialho.tveebot.downloader.DownloadReference

internal class LibTorrentReference(private val hash: Sha1Hash) : DownloadReference {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LibTorrentReference

        return hash == other.hash
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }

    override fun toString(): String {
        return "LibTorrentReference(hash=$hash)"
    }

}