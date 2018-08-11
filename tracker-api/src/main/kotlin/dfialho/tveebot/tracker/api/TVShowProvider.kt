package dfialho.tveebot.tracker.api

/**
 * A TV show provider exposes methods to fetch video files for TV show episodes from a repository.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TVShowProvider {

    /**
     * Fetches all TV Shows from the provider.
     */
    fun fetchTVShows(): List<TVShow>

    /**
     * Fetches the most recent episodes available for [tvShow]. The number of episodes returned by this method is
     * specific to each implementation.
     */
    fun fetchEpisodes(tvShow: TVShow): List<EpisodeFile>
}