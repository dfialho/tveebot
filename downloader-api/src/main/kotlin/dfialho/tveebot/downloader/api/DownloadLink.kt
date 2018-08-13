package dfialho.tveebot.downloader.api

/**
 * A [DownloadLink] can be provided a [DownloadEngine] to start a download.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface DownloadLink {

    val raw: String

    fun download(downloadEngine: DownloadEngine): DownloadHandle
}