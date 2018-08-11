package dfialho.tveebot.tracker.api

import java.time.Instant

/**
 * Representation of a video file for an [Episode].
 *
 * @property episode The episode itself
 * @property quality The video quality of the episode file
 * @property link The link from which the video file can be downloaded
 * @property publishedDate The date when the video file was published
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
data class EpisodeFile(
    val episode: Episode,
    val quality: VideoQuality,
    val link: String,
    val publishedDate: Instant
)

