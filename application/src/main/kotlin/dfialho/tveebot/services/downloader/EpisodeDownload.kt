package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow

/**
 * Data class to represent an episode that is being downloaded.
 *
 * @property reference Reference of the download corresponding to the episode
 * @property tvShow TV show the episode belongs to
 * @property episode Episode being downloaded
 * @author David Fialho (dfialho@protonmail.com)
 */
data class EpisodeDownload(
    val reference: DownloadReference,
    val tvShow: TVShow,
    val episode: EpisodeFile
)
