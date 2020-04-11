package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.VideoFile
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.utils.Result

class EpisodeLedgerRepository(private val repository: TVeebotRepository) : EpisodeLedger {

    // FIXME incomplete
    override fun appendOrUpdate(episodeFile: EpisodeFile): Result {

        return repository.transaction {
            val episode = episodeFile.episodes[0]
            val videoFile: VideoFile? = findEpisodeLatestFile(episode.id)

            if (videoFile == null || episodeFile.file.publishDate.isAfter(videoFile.publishDate)) {
                repository.insert(episodeFile)
                Result.Success
            } else {
                Result.Failure
            }
        }
    }
}