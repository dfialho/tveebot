package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackerRepository
import java.util.*

class InMemoryTrackerRepository : TrackerRepository {

    private data class TVShowEntry(val tvShow: TVShow, var tracked: Boolean)

    private val tvShows = mutableMapOf<UUID, TVShowEntry>()
    private val episodeFiles = mutableMapOf<UUID, MutableList<EpisodeFile>>()

    override fun put(tvShow: TVShow, tracked: Boolean) {
        tvShows[tvShow.id] = TVShowEntry(tvShow, tracked)
    }

    override fun putAll(tvShows: List<TVShow>) {
        this.tvShows.putAll(tvShows.associate { it.id to TVShowEntry(it, tracked = false) })
    }

    override fun findAllTVShows(): List<TVShow> = tvShows.values
        .map { it.tvShow }

    override fun findTVShows(tracked: Boolean): List<TVShow> = tvShows.values
        .filter { it.tracked == tracked }
        .map { it.tvShow }

    override fun setTracked(tvShowUUID: UUID, tracked: Boolean) {
        tvShows[tvShowUUID]?.tracked = tracked
    }

    override fun put(tvShow: TVShow, episodeFile: EpisodeFile) {
        episodeFiles
            .getOrPut(tvShow.id) { mutableListOf(episodeFile) }
            .add(episodeFile)
    }

    override fun findAllVideosFor(tvShow: TVShow): List<EpisodeFile> {
        return episodeFiles[tvShow.id] ?: emptyList()
    }
}