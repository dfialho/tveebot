package dfialho.tveebot.tracker.api

import java.util.*

/**
 * A tracked TV show contains the information of a [TVShow] plus extra properties, such as, the video [quality] with
 * which episodes of this TV show should be downloaded.
 *
 * @property tvShow reference to the TV show being tracked
 * @property quality defines the video [quality] with which episodes of this TV show should be downloaded
 * @author David Fialho (dfialho@protonmail.com)
 */
data class TrackedTVShow(private val tvShow: TVShow, val quality: VideoQuality) {
    val title: String get() = tvShow.title
    val id: UUID get() = tvShow.id

    /**
     * Returns a representation of this tracked tv show as a [TVShow].
     */
    fun toTVShow(): TVShow = tvShow
}
