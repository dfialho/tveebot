package dfialho.tveebot.tracker.api.models

import dfialho.tveebot.application.api.VideoQuality
import java.time.Instant

data class EpisodeFile(
    val episode: Episode,
    val quality: VideoQuality,
    val link: String,
    val publishDate: Instant
)

val EpisodeFile.tvShow get() = episode.tvShow
