package dfialho.tveebot.tracker.api

import java.util.*

/**
 * Representation of a TV show.
 * Equals is overridden here. Two TV shows are considered equal if they have different [tracked] values.
 *
 * @property title Title of the TV show.
 * @property id Identifier of the TV show. This is unique for each TV Show.
 * @property quality Defines the video [quality] with which episodes of this TV show should be downloaded.
 * @property tracked Flag indicating the TV show is tracked or not.
 * @author David Fialho (dfialho@protonmail.com)
 */
data class TVShow(
    val id: UUID,
    val title: String,
    val quality: VideoQuality = VideoQuality.default(),
    val tracked: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TVShow

        if (id != other.id) return false
        if (title != other.title) return false
        if (quality != other.quality) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + quality.hashCode()
        return result
    }
}
