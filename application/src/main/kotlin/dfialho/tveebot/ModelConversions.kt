package dfialho.tveebot

import dfialho.tveebot.application.api.EpisodeEntity
import dfialho.tveebot.application.api.TVShowEntity
import dfialho.tveebot.tracker.api.models.*

@Suppress("NOTHING_TO_INLINE")
inline fun TVShowEntity.toTVShow(): TVShow {
    return TVShow(id, title, quality)
}

@Suppress("NOTHING_TO_INLINE")
inline fun EpisodeEntity.toEpisodeFile(): EpisodeFile {
    return EpisodeFile(this.toEpisode(), quality, link, publishDate)
}

@Suppress("NOTHING_TO_INLINE")
inline fun EpisodeEntity.toEpisode(): Episode {
    return Episode(title, season, number)
}

@Suppress("NOTHING_TO_INLINE")
inline fun tvShowEntityOf(tvShow: TVShow, tracked: Boolean = false): TVShowEntity {
    return TVShowEntity(tvShow.id, tvShow.title, tvShow.quality, tracked)
}

@Suppress("NOTHING_TO_INLINE")
inline fun tvShowEpisodeOf(tvShow: TVShow, episode: Episode): TVShowEpisode {
    return TVShowEpisode(tvShow.id, tvShow.title, episode.title, episode.season, episode.number)
}

@Suppress("NOTHING_TO_INLINE")
inline fun tvShowEpisodeFileOf(tvShow: TVShow, episode: EpisodeFile): TVShowEpisodeFile {
    return with(episode) {
        TVShowEpisodeFile(tvShowEpisodeOf(tvShow, this.toEpisode()), quality, link, publishDate)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun tvShowEpisodeFileOf(tvShow: TVShowEntity, episode: EpisodeEntity): TVShowEpisodeFile {
    return TVShowEpisodeFile(
        tvShowEpisodeOf(tvShow.toTVShow(), episode.toEpisode()),
        episode.quality,
        episode.link,
        episode.publishDate
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun TVShowEpisodeFile.toEpisodeFile(): EpisodeFile {
    return EpisodeFile(Episode(title, season, number), quality, link, publishDate)
}
