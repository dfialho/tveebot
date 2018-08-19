package dfialho.tveebot.data.tracker

import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.EpisodeRecorder
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.isMoreRecentThan
import org.springframework.stereotype.Repository

/**
 * Implementation of an [EpisodeRecorder] backed by the [TrackerRepository].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@Repository
class EpisodeRecorderRepository(private val repository: TrackerRepository) : EpisodeRecorder {

    override fun getTVShows(): List<TVShow> = repository.findTrackedTVShows()

    override fun getEpisodes(tvShow: TVShow): List<EpisodeFile> = repository.findEpisodesFrom(tvShow.id)

    override fun putOrUpdateIfMoreRecent(tvShow: TVShow, episode: EpisodeFile): Boolean {
        return repository.putOrUpdateIf(tvShow.id, episode) { oldEpisode, newEpisode -> newEpisode isMoreRecentThan oldEpisode }
    }
}