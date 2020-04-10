package dfialho.tveebot.app.api.models

data class TVShowEntity(
    val tvShow: TVShow,
    val tracked: Boolean = false,
    val videoQuality: VideoQuality = VideoQuality.default()
)
