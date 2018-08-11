package dfialho.tveebot.tracker.api

interface TrackerRepository {

    fun put(tvShow: TVShow)

    fun findAllTVShows(): List<TVShow>

    fun put(tvShow: TVShow, episode: Episode)

    fun findAllEpisodesFrom(tvShow: TVShow): List<Episode>
}