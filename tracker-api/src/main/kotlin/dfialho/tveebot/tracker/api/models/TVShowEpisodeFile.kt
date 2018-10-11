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

    fun copy(
        tvShowID: TVShowID = this.tvShowID,
        tvShowTitle: String = this.tvShowTitle,
        title: String = this.title,
        season: Int = this.season,
        number: Int = this.number,
        quality: VideoQuality = this.quality,
        link: String = this.link,
        publishDate: Instant = this.publishDate
    ): TVShowEpisodeFile {

        return TVShowEpisodeFile(
            TVShowEpisode(tvShowID, tvShowTitle, title, season, number),
            quality,
            link,
            publishDate
        )
    }

    /**
     * Returns a representation of this file as a [TVShowEpisode].
     */
    fun toTVShowEpisode(): TVShowEpisode = episode
}
