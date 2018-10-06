package dfialho.tveebot

import dfialho.tveebot.data.models.EpisodeEntity
import dfialho.tveebot.data.models.TVShowEntity
import dfialho.tveebot.tracker.api.models.Episode
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile

@Suppress("NOTHING_TO_INLINE")
inline fun TVShowEntity.toTVShow(): TVShow {
    return TVShow(id, title, quality)
}

@Suppress("NOTHING_TO_INLINE")
inline fun EpisodeEntity.toEpisodeFile(): EpisodeFile {
    return EpisodeFile(Episode(title, season, number), quality, link, publishDate)
}

@Suppress("NOTHING_TO_INLINE")
inline fun tvShowEntityFrom(tvShow: TVShow, tracked: Boolean = false): TVShowEntity {
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
inline fun TVShowEpisodeFile.toEpisodeFile(): EpisodeFile {
    return EpisodeFile(Episode(title, season, number), quality, link, publishDate)
}