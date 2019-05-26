package dfialho.tveebot.tracker.api

import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.TVShow
import java.io.IOException

/**
 * A TV show provider is an entity which is able to provide information about one or more TV shows.
 *
 * It is sort of a database which contains data about TV shows, including information details about
 * recent episodes corresponding to each TV show. As with any database, it does not necessarily contain information
 * about all episodes of a TV show. Still, it is expected to be able to provide information about the most recent
 * episodes of each TV show.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TVShowProvider {

    /**
     * Fetches all TV Shows from the provider.
     *
     * @throws IOException if an error occurs when trying to reach the provider
     */
    fun fetchTVShows(): List<TVShow>

    /**
     * Fetches the most recent episodes available for [tvShow]. The number of episodes returned by this method is
     * specific to each implementation, but it is expected to at least return the most recent episodes.
     *
     * @throws IOException if an error occurs when trying to reach the provider
     */
    fun fetchEpisodes(tvShow: TVShow): List<EpisodeFile>
}