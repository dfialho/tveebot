package dfialho.tveebot.tracker.api

import java.util.*

/**
 * Representation of a TV show.
 *
 * @property title The title of the TV show
 * @property id The identifier of the TV show. This is unique for each TV Show.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
data class TVShow(
    val title: String,
    val id: UUID = UUID.randomUUID()
)