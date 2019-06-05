package dfialho.tveebot.application.api

import java.time.Instant

data class EpisodeEntity(
    val id: ID,
    val season: Int,
    val number: Int,
    val title: String,
    val link: String,
    val publishDate: Instant,
    val quality: VideoQuality = VideoQuality.default(),
    val state: EpisodeState
)
