package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.VideoQuality

/**
 * Data class containing the identifiers of an [EpisodeFile].
 *
 * @property season The season number of the episode
 * @property number The number of the episode within its season
 * @property quality The quality of the video file
 */
internal data class EpisodeIdentifier(
    val season: Int,
    val number: Int,
    val quality: VideoQuality
)

/**
 * Extension property to obtain [EpisodeIdentifier] for an [EpisodeFile].
 */
internal val EpisodeFile.identifier: EpisodeIdentifier
    get() = EpisodeIdentifier(this.season, this.number, this.quality)
