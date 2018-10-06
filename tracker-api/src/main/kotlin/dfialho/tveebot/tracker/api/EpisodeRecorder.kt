package dfialho.tveebot.tracker.api

import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile

/**
 * Entity responsible for keeping track of the episodes found for each TV show being tracked.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface EpisodeRecorder {

    /**
     * Returns a list containing all TV shows currently being tracked.
     */
    fun getTVShows(): List<TVShow>

    /**
     * Inserts the [episode] into the recorder if it does not exist yet. Otherwise, it updates an existing episode file
     * if an only if [episode] file is more recent than the existing episode file.
     */
    fun putOrUpdateIfMoreRecent(episode: TVShowEpisodeFile): Boolean
}