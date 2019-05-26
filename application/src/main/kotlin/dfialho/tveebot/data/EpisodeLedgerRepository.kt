package dfialho.tveebot.data

import dfialho.tveebot.episodeFileOf
import dfialho.tveebot.toTVShow
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.utils.Result
import dfialho.tveebot.utils.orElse

class EpisodeLedgerRepository(private val repository: TrackerRepository) : EpisodeLedger {

    override fun appendOrUpdate(episode: EpisodeFile): Result {
        // catch exception if it exists and update it if it is more recent
        // the reason for doing this here instead of at the tracker engine level
        // is that the "update if more recent" semantics is related to the ledger
        // and should be shared by all users of the episodeFile ledger

        return repository.put(episode)
            .orElse { repository.updateIf(episode) { old, new -> new isMoreRecentThan old } }
    }

    override fun toList(): List<EpisodeFile> {
        return repository.findEpisodesByTVShow()
            .flatMap { (tvShowEntity, episodes) -> episodes.map { episodeFileOf(tvShowEntity.toTVShow(), it) } }
    }
}

/**
 * Returns true if this episode file is more recent than [other], or false if otherwise.
 */
private infix fun EpisodeFile.isMoreRecentThan(other: EpisodeFile): Boolean {
    return this.publishDate.isAfter(other.publishDate)
}