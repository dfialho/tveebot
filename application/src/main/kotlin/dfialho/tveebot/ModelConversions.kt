package dfialho.tveebot

import dfialho.tveebot.application.api.EpisodeEntity
import dfialho.tveebot.application.api.TVShowEntity
import dfialho.tveebot.tracker.api.models.Episode
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.VideoQuality

@Suppress("NOTHING_TO_INLINE")
inline fun TVShowEntity.toTVShow(): TVShow {
    return TVShow(id, title)
}

@Suppress("NOTHING_TO_INLINE")
inline fun tvShowEntityOf(tvShow: TVShow, tracked: Boolean = false): TVShowEntity {
    return TVShowEntity(tvShow.id, tvShow.title, VideoQuality.SD, tracked)
}

@Suppress("NOTHING_TO_INLINE")
inline fun episodeFileOf(tvShow: TVShow, episode: EpisodeEntity) = EpisodeFile(
    episode = Episode(
        tvShow = tvShow,
        season = episode.season,
        number = episode.number,
        title = episode.title
    ),
    quality = episode.quality,
    link = episode.link,
    publishDate = episode.publishDate
)

@Suppress("NOTHING_TO_INLINE")
inline fun episodeFileOf(tvShow: TVShowEntity, episode: EpisodeEntity): EpisodeFile {
    return episodeFileOf(TVShow(tvShow.id, tvShow.title), episode)
}
