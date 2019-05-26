package dfialho.tveebot.services.models

import dfialho.tveebot.tracker.api.models.EpisodeFile
import java.nio.file.Path

data class DownloadNotification(
    val episodeFile: EpisodeFile,
    val savePath: Path
)