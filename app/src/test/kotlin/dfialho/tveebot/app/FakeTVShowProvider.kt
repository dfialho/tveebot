package dfialho.tveebot.app

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShow
import dfialho.tveebot.tracker.api.TVShowProvider

class FakeTVShowProvider(tvShows: List<ProvidedTVShow>) : TVShowProvider {

    private val episodes = tvShows.associateBy({ it.tvShow.id }, { it.episodeFiles.toMutableList() }).toMutableMap()
    private val tvShows = tvShows.associateBy({ it.tvShow.id }, { it.tvShow }).toMutableMap()

    override fun fetchEpisodes(tvShow: TVShow): List<EpisodeFile> {
        return episodes[tvShow.id] ?: throw IllegalStateException("$tvShow not found")
    }

    override fun fetchTVShow(tvShowId: String): TVShow? {
        return tvShows[tvShowId]
    }

    fun addEpisode(tvShow: TVShow, episodeFile: EpisodeFile) {
        require(episodeFile.episodes.all { it.tvShow.id == tvShow.id }) { "All episodes must be associated with input tv show" }

        episodes[tvShow.id]?.add(episodeFile)
    }
}

data class ProvidedTVShow(val tvShow: TVShow, val episodeFiles: List<EpisodeFile>)

fun fakeTVShowProvider(vararg providedTVShows: ProvidedTVShow): FakeTVShowProvider {

    return FakeTVShowProvider(providedTVShows.map { tvShow ->
        tvShow.copy(episodeFiles = tvShow.episodeFiles.map { episodeFile ->
            episodeFile.copy(episodes = episodeFile.episodes.map {
                it.copy(tvShow = tvShow.tvShow)
            })
        })
    })
}
