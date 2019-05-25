package dfialho.tveebot.services.models

import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import java.nio.file.Path

data class FinishedDownloadNotification(
    val episode: TVShowEpisodeFile,
    val savePath: Path
)