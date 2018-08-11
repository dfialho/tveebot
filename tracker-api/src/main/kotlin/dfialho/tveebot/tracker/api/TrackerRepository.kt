package dfialho.tveebot.tracker.api

interface TrackerRepository {

    fun put(tvShow: TVShow)

    fun findAllTVShows(): List<TVShow>

    fun remove(tvShow: TVShow)

    fun put(tvShow: TVShow, episodeFile: EpisodeFile)

    fun findAllVideosFor(tvShow: TVShow): List<EpisodeFile>
}