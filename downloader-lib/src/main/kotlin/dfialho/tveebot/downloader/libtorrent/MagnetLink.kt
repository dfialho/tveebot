package dfialho.tveebot.downloader.libtorrent

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadLink

/**
 * [DownloadLink] represented by magnet link.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class MagnetLink(private val link: String) : DownloadLink {

    override val raw: String
        get() = link

    override fun download(downloadEngine: DownloadEngine): DownloadHandle = downloadEngine.add(link)
}