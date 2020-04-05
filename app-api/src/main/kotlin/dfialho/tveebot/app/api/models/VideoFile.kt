package dfialho.tveebot.app.api.models

import java.time.Instant

data class VideoFile(
    val link: String,
    val quality: VideoQuality,
    val publishDate: Instant
)
