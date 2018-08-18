package dfialho.tveebot.tracker.api

/**
 * Representation of a TV show episode.
 *
 * @property title The title of the episode
 * @property season The number of the season to which the episode belongs to
 * @property number The number of the episode in its corresponding season
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
data class Episode(
    val title: String,
    val season: Int,
    val number: Int
)
