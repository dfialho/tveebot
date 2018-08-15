package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.EpisodeFile
import java.util.*

data class EpisodeDownload(
    val reference: DownloadReference,
    val tvShowUUID: UUID,
    val episodeFile: EpisodeFile
)
