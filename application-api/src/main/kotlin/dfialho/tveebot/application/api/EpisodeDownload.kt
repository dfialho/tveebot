package dfialho.tveebot.application.api

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile

/**
 * Data class to represent an episode that is being downloaded.
 *
 * @property reference Reference of the download corresponding to the episode
 * @property episode Episode being downloaded
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
data class EpisodeDownload(
    val reference: DownloadReference,
    val episode: TVShowEpisodeFile
)
