package dfialho.tveebot.tracker.api.models

import dfialho.tveebot.application.api.ID

/**
 * Representation of a TV show.
 *
 * @property title Title of the TV show.
 * @property id Identifier of the TV show. This is unique for each TV Show.
 */
data class TVShow(
    val id: ID,
    val title: String
)
