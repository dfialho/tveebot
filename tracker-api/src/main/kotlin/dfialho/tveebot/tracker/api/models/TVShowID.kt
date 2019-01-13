package dfialho.tveebot.tracker.api.models

import java.util.*

inline class TVShowID(val value: String)

fun randomTVShowID(): TVShowID = TVShowID(UUID.randomUUID().toString())
