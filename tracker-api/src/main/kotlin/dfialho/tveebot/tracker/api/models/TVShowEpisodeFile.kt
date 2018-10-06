package dfialho.tveebot.tracker.api.models

import java.time.Instant

data class TVShowEpisodeFile(
    private val episode: TVShowEpisode,
    val quality: VideoQuality,
    val link: String,
    val publishDate: Instant

) {
    val tvShowID: TVShowID get() = episode.tvShowID
    val tvShowTitle: String get() = episode.tvShowTitle
    val title: String get() = episode.title
    val season: Int get() = episode.season
    val number: Int get() = episode.number

    /**
     * Returns a representation of this file as a [TVShowEpisode].
     */
    fun toTVShowEpisode(): TVShowEpisode = episode
}
