package dfialho.tveebot.downloader.api

/**
 * Describes the state of a download.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
enum class DownloadState {

    /**
     * The download has not started yet, adn it is currently checking existing files to avoid downloading parts of the
     * files that have already been downloaded.
     */
    SCANNING_FILES,

    /**
     * The download is trying to download required metadata.
     */
    DOWNLOADING_METADATA,

    /**
     * The download is in progress.
     */
    DOWNLOADING,

    /**
     * The download has finished downloading, and it has all the pieces it required.
     */
    FINISHED,

    /**
     * The download is in an unknown state.
     */
    UNKNOWN;

}