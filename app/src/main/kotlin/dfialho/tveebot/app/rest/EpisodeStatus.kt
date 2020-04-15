package dfialho.tveebot.app.rest

import dfialho.tveebot.app.api.models.State

data class EpisodeStatus(
    val season: Int,
    val number: Int,
    val title: String,
    val state: State
)
