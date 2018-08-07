package dfialho.tveebot.tracker.api

interface TrackerRepository {

    fun put(tvShow: TVShow)

    fun findAllTVShows(): List<TVShow>

    fun put(episode: Episode)

    fun findAllEpisodes(): List<Episode>

    fun findAllEpisodesFrom(tvShow: TVShow): List<Episode>
}