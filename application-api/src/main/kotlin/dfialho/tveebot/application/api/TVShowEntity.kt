package dfialho.tveebot.application.api

import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.VideoQuality

/**
 * Representation of a TV show entity.
 *
 * @property title Title of the TV show.
 * @property id Identifier of the TV show. This is unique for each TV Show.
 * @property quality Defines the video [quality] with which episodes of this TV show should be downloaded.
 * @property tracked Flag indicating the TV show is tracked or not.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
data class TVShowEntity(
    val id: ID,
    val title: String,
    val quality: VideoQuality = VideoQuality.default(),
    val tracked: Boolean = false
)
