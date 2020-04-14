package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.VideoFile
import dfialho.tveebot.tracker.api.EpisodeLedger

class EpisodeLedgerRepository(private val repository: TVeebotRepository) : EpisodeLedger {

    override fun appendOrUpdate(episodeFile: EpisodeFile): Boolean {

        return repository.transaction {

            val episode = episodeFile.episodes[0]
            val videoFile: VideoFile? = findEpisodeLatestFile(episode.id, episodeFile.file.quality)

            if (videoFile == null || episodeFile.file.publishDate.isAfter(videoFile.publishDate)) {
                repository.insert(episodeFile)
                true
            } else {
                false
            }
        }
    }
}
