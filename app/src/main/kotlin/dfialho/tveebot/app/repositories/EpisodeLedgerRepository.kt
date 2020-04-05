package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.utils.Result

class EpisodeLedgerRepository(private val repository: TVeebotRepository) : EpisodeLedger {

    // FIXME incomplete
    override fun appendOrUpdate(episodeFile: EpisodeFile): Result {
        repository.insert(episodeFile)
        return Result.Success
    }

    override fun toList(): List<EpisodeFile> {
        TODO("Not yet implemented")
    }
}