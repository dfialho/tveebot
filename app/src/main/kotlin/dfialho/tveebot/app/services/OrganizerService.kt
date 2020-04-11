package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeEntity
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.lib.EpisodeDownloadPackage
import mu.KLogging
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OrganizerService(
    private val library: TVShowLibrary,
    private val repository: TVeebotRepository,
    private val eventBus: EventBus
) : Service {

    companion object : KLogging()

    private val executor = Executors.newSingleThreadExecutor()

    override fun start() {
        subscribe<Event.DownloadFinished>(eventBus) {
            store(it.episode, it.savePath)
        }
    }

    override fun stop() {

        logger.debug { "Shutting down executor" }
        with(executor) {
            shutdown()

            try {
                awaitTermination(30, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                logger.warn { "Failed to stop Organizer Service cleanly: Timed out waiting for task to finish." }
            }
        }
    }

    fun store(episodeFile: EpisodeFile, fileLocation: Path) {

        executor.submit {
            try {
                logger.debug { "Storing episode in library: $episodeFile" }
                val storePath = library.store(episodeFile.episodes, EpisodeDownloadPackage(fileLocation))

                repository.transaction {
                    episodeFile.episodes
                        .map { EpisodeEntity(it, State.STORED) }
                        .forEach { update(it) }
                }

                logger.info { "Stored episode file for episode(s): ${episodeFile.episodes}" }
                eventBus.fire(Event.FileStored(episodeFile, storePath))

            } catch (e: Throwable) {
                logger.error(e) { "Failed to store episode in library: ${episodeFile.episodes}" }
            }
        }
    }
}
