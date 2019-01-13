package dfialho.tveebot.downloader.api

/**
 * A reference for a download. Downloads are identified by a reference within a [DownloadEngine].
 *
 * @property value Reference represented as a string.
 * @author David Fialho (dfialho@protonmail.com)
 */
inline class DownloadReference(val value: String) {
    override fun toString(): String {
        return value
    }
}
