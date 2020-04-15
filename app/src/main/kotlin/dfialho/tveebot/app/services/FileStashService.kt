package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.components.FileStash
import dfialho.tveebot.app.events.*
import dfialho.tveebot.app.repositories.TVeebotRepository
import mu.KLogging
import java.nio.file.Path

class FileStashService(
    private val repository: TVeebotRepository,
    private val stash: FileStash,
    private val eventBus: EventBus
) : Service {

    companion object : KLogging()

    override fun start() {

        subscribe<Event.DownloadFinished>(eventBus) {
            stash(it.savePath, it.episode)
        }

        stash.takeEach { fileId, path ->

            val episodeFile = repository.findEpisodeFile(fileId)

            if (episodeFile == null) {
                logger.warn { "No episode found for file in stash: $path" }
            } else {
                fire(eventBus, Event.FileStashed(episodeFile, path))
            }
        }
    }

    override fun stop() {
        unsubscribe<Event.DownloadFinished>(eventBus)
    }

    private fun stash(filePath: Path, episodeFile: EpisodeFile) {
        stash.put(episodeFile.file.id, filePath)
        fire(eventBus, Event.FileStashed(episodeFile, filePath))
    }
}
