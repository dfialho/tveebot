package dfialho.tveebot.tracker.api.models

import java.util.UUID.randomUUID

typealias EpisodeID = String
typealias TVShowID = String

@Suppress("NOTHING_TO_INLINE")
inline fun tvShowIDFromString(id: String): TVShowID = id

fun randomTVShowID(): TVShowID = randomUUID().toString()
