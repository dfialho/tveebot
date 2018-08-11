package dfialho.tveebot.tracker.api

import java.util.*

interface TrackerRepository {

    fun put(tvShow: TVShow, tracked: Boolean = false)

    fun putAll(tvShows: List<TVShow>)

    fun findAllTVShows(): List<TVShow>

    fun findTVShows(tracked: Boolean): List<TVShow>

    fun setTracked(tvShowUUID: UUID, tracked: Boolean = true)

    fun put(tvShow: TVShow, episodeFile: EpisodeFile)

    fun findAllVideosFor(tvShow: TVShow): List<EpisodeFile>
}