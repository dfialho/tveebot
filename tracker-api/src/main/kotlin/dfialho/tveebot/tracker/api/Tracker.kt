package dfialho.tveebot.tracker.api

interface TrackerService {

    var provider: TVShowProvider

    fun start()

    fun stop()

    fun add(tvShow: TVShow)

    fun remove(tvShow: TVShow)

    fun removeTVShow(id: String)
}

interface TrackerRepository {

    fun put(tvShow: TVShow)

    fun findAllTVShows(): List<TVShow>

    fun put(episode: Episode)

    fun findAllEpisodes(): List<Episode>

    fun findAllEpisodesFrom(tvShow: TVShow): List<Episode>
}