package dfialho.tveebot.downloader.libtorrent

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadLink
import java.nio.file.Path

/**
 * [DownloadLink] represented by torrent file.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class TorrentFile(private val path: Path) : DownloadLink {

    override val raw: String by lazy { path.toAbsolutePath().toString() }

    override fun download(downloadEngine: DownloadEngine): DownloadHandle = downloadEngine.add(path)

}