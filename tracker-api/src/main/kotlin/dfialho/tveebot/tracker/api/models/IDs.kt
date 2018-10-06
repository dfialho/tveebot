package dfialho.tveebot.tracker.api.models

import java.util.*
import java.util.UUID.randomUUID

typealias EpisodeID = String
typealias TVShowID = UUID

fun tvShowIDFromString(id: String): TVShowID = UUID.fromString(id)

fun randomTVShowID(): TVShowID = randomUUID()
