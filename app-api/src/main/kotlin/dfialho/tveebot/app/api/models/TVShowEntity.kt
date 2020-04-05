package dfialho.tveebot.app.api.models

data class TVShowEntity(
    val tvShow: TVShow,
    val tracked: Boolean,
    val videoQuality: VideoQuality
)
