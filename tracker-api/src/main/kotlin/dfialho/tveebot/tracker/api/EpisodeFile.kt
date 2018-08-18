package dfialho.tveebot.tracker.api

import java.time.Instant

/**
 * Representation of a video file for an [Episode].
 *
 * @property episode The episode itself
 * @property quality The video quality of the episode file
 * @property link The link from which the video file can be downloaded
 * @property publishedDate The date when the video file was published
 * @author David Fialho (dfialho@protonmail.com)
 */
data class EpisodeFile(
    private val episode: Episode,
    val quality: VideoQuality,
    val link: String,
    val publishedDate: Instant
) {
    val title: String get() = episode.title
    val season: Int get() = episode.season
    val number: Int get() = episode.number

    /**
     * Returns a representation of this file as an [Episode].
     */
    fun toEpisode(): Episode = episode

    /**
     * Returns a pretty string representation of the episode.
     */
    fun toPrettyString(): String {
        return "${season}x%02d - $title ($quality)".format(number)
    }
}

/**
 * Returns true if this episode file is more recent than [other], or false if otherwise.
 */
infix fun EpisodeFile.isMoreRecentThan(other: EpisodeFile): Boolean {
    return this.publishedDate.isAfter(other.publishedDate)
}
