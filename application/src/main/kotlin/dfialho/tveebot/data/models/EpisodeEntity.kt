package dfialho.tveebot.data.models

import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.VideoQuality
import java.time.Instant

data class EpisodeEntity(
    val id: ID,
    val season: Int,
    val number: Int,
    val title: String,
    val link: String,
    val publishDate: Instant,
    val quality: VideoQuality = VideoQuality.default(),
    val state: EpisodeState = EpisodeState.AVAILABLE
)
