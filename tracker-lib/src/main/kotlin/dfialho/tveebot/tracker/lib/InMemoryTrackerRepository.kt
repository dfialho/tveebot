package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackerRepository
import java.util.*

class InMemoryTrackerRepository : TrackerRepository {

    private val tvShows = mutableSetOf<TVShow>()
    private val episodeFiles = mutableMapOf<UUID, MutableList<EpisodeFile>>()

    override fun put(tvShow: TVShow) {
        tvShows += tvShow
    }

    override fun findAllTVShows(): List<TVShow> = tvShows.toList()

    override fun remove(tvShow: TVShow) {
        episodeFiles.remove(tvShow.id)
        tvShows.remove(tvShow)
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