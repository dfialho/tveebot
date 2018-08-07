package dfialho.tveebot.tracker.api

interface TrackerService {

    var provider: TVShowProvider

    fun start()

    fun stop()

    fun add(tvShow: TVShow)

    fun remove(tvShow: TVShow)

    fun removeTVShow(id: String)
}
