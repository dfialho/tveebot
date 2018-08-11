package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.VideoQuality

internal data class EpisodeIdentifier(
    val season: Short,
    val number: Short,
    val quality: VideoQuality
)

internal val EpisodeFile.identifier: EpisodeIdentifier
    get() = EpisodeIdentifier(this.episode.season, this.episode.number, this.quality)
