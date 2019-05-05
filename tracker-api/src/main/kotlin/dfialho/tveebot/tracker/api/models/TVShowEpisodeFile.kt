package dfialho.tveebot.tracker.api.models

import java.time.Instant

data class TVShowEpisodeFile(
    val tvShowID: ID,
    val tvShowTitle: String,
    val title: String,
    val season: Int,
    val number: Int,
    val quality: VideoQuality,
    val link: String,
    val publishDate: Instant
) {
    constructor(episode: TVShowEpisode, quality: VideoQuality, link: String, publishDate: Instant) : this(
        episode.tvShowID,
        episode.tvShowTitle,
        episode.title,
        episode.season,
        episode.number,
        quality,
        link,
        publishDate
    )

    /**
     * Returns a representation of this file as a [TVShowEpisode].
     */
    fun toTVShowEpisode(): TVShowEpisode = TVShowEpisode(tvShowID, tvShowTitle, title, season, number)
}
