package dfialho.tveebot.data

import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.isMoreRecentThan
import dfialho.tveebot.tvShowEpisodeFileOf
import dfialho.tveebot.utils.Result
import dfialho.tveebot.utils.orElse

class EpisodeLedgerRepository(private val repository: TrackerRepository) : EpisodeLedger {

    override fun appendOrUpdate(episode: TVShowEpisodeFile): Result {
        // catch exception if it exists and update it if it is more recent
        // the reason for doing this here instead of at the tracker engine level
        // is that the "update if more recent" semantics is related to the ledger
        // and should be shared by all users of the episode ledger

        return repository.put(episode).orElse { repository.updateIf(episode) { old, new -> new isMoreRecentThan old } }
    }

    override fun toList(): List<TVShowEpisodeFile> {
        return repository.findEpisodesByTVShow()
            .flatMap { (tvShow, episodes) -> episodes.map { tvShowEpisodeFileOf(tvShow, it) } }
    }
}


