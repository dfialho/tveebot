package dfialho.tveebot.data

import dfialho.tveebot.toEpisodeFile
import dfialho.tveebot.toTVShow
import dfialho.tveebot.tracker.api.EpisodeRecorder
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.isMoreRecentThan

/**
 * Implementation of an [EpisodeRecorder] backed by the [TrackerRepository].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
// TODO Replace with TrackingList and EpisodeLedger
class EpisodeRecorderRepository(private val repository: TrackerRepository) : EpisodeRecorder {

    override fun getTVShows(): List<TVShow> = repository.findTrackedTVShows().map { it.toTVShow() }

    override fun putOrUpdateIfMoreRecent(episode: TVShowEpisodeFile): Boolean {
        return repository.putOrUpdateIf(episode.tvShowID, episode.toEpisodeFile()) {
            oldEpisode, newEpisode -> newEpisode isMoreRecentThan oldEpisode
        }
    }
}