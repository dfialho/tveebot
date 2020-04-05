package dfialho.tveebot.app.events

import dfialho.tveebot.app.api.models.EpisodeFile
import java.nio.file.Path

sealed class Event {
    data class VideoFileFound(val episode: EpisodeFile) : Event()
    data class DownloadStarted(val episode: EpisodeFile) : Event()
    data class DownloadFinished(val episode: EpisodeFile, val savePath: Path) : Event()
    data class FileStored(val episode: EpisodeFile, val savePath: Path) : Event()
}
