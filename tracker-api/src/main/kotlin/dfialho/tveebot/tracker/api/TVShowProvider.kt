package dfialho.tveebot.tracker.api

interface TVShowProvider {

    fun fetchEpisodes(tvShow: TVShow): List<EpisodeVideo>
}