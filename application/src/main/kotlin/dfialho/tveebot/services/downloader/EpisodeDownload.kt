package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.EpisodeFile
import java.util.*

/**
 * Data class to represent an episode that is being downloaded.
 *
 * @property reference Reference of the download corresponding to the episode
 * @property tvShowUUID ID of the TV show the episode belongs to
 * @property episodeFile Episode file being downloaded
 * @author David Fialho (dfialho@protonmail.com)
 */
data class EpisodeDownload(
    val reference: DownloadReference,
    val tvShowUUID: UUID,
    val episodeFile: EpisodeFile
)
