package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackerRepository
import java.util.*

class InMemoryTrackerRepository : TrackerRepository {

    private val tvShows = mutableSetOf<TVShow>()
    private val episodes = mutableMapOf<UUID, MutableList<Episode>>()

    override fun put(tvShow: TVShow) {
        tvShows += tvShow
    }

    override fun findAllTVShows(): List<TVShow> = tvShows.toList()

    override fun put(tvShow: TVShow, episode: Episode) {
        episodes
            .getOrPut(tvShow.id) { mutableListOf(episode) }
            .add(episode)
    }

    override fun findAllEpisodesFrom(tvShow: TVShow): List<Episode> {
        return episodes[tvShow.id] ?: emptyList()
    }
}