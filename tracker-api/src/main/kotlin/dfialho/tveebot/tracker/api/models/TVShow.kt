package dfialho.tveebot.tracker.api.models

/**
 * Representation of a TV show.
 *
 * @property title Title of the TV show.
 * @property id Identifier of the TV show. This is unique for each TV Show.
 * @property quality Defines the video [quality] with which episodes of this TV show should be downloaded.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
data class TVShow(
    val id: TVShowID,
    val title: String,
    val quality: VideoQuality = VideoQuality.default()
)
