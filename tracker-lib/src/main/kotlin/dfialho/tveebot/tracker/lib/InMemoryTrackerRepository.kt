package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackerRepository
import java.util.*

class InMemoryTrackerRepository : TrackerRepository {

    private data class TVShowEntry(val tvShow: TVShow, val tracked: Boolean)

    private val tvShows = mutableSetOf<TVShowEntry>()
    private val episodeFiles = mutableMapOf<UUID, MutableList<EpisodeFile>>()

    override fun put(tvShow: TVShow, tracked: Boolean) {
        tvShows += TVShowEntry(tvShow, tracked)
    }

    override fun putAll(tvShows: List<TVShow>) {
        this.tvShows.addAll(tvShows.map { TVShowEntry(it, tracked = false) })
    }

    override fun findAllTVShows(): List<TVShow> = tvShows
        .map { it.tvShow }

    override fun findTVShows(tracked: Boolean): List<TVShow> = tvShows
        .filter { it.tracked == tracked }
        .map { it.tvShow }

    override fun put(tvShow: TVShow, episodeFile: EpisodeFile) {
        episodeFiles
            .getOrPut(tvShow.id) { mutableListOf(episodeFile) }
            .add(episodeFile)
    }

    override fun findAllVideosFor(tvShow: TVShow): List<EpisodeFile> {
        return episodeFiles[tvShow.id] ?: emptyList()
    }
}