package dfialho.tveebot.tracker.api.models

data class TVShowEpisode(
    val tvShowID: ID,
    val tvShowTitle: String,
    val title: String,
    val season: Int,
    val number: Int
)
