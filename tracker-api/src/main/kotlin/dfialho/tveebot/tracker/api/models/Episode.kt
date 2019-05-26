package dfialho.tveebot.tracker.api.models

/**
 * Representation of a TV show episode.
 *
 * @property tvShow The TV show this episode belongs to
 * @property season The number of the season to which the episode belongs to
 * @property number The number of the episode in its corresponding season
 * @property title The title of the episode
 */
data class Episode(
    val tvShow: TVShow,
    val season: Int,
    val number: Int,
    val title: String
)
