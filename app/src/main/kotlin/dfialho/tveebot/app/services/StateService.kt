package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeEntity
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.events.unsubscribe
import dfialho.tveebot.app.repositories.TVeebotRepository
import mu.KLogging

class StateService(
    private val repository: TVeebotRepository,
    private val eventBus: EventBus
) : Service {

    companion object : KLogging()

    override fun start() {

        subscribe<Event.EpisodeFileFound>(eventBus) {
            updateState(it.episode, State.FOUND)
        }

        subscribe<Event.DownloadStarted>(eventBus) {
            updateState(it.episode, State.DOWNLOADING)
        }

        subscribe<Event.DownloadFinished>(eventBus) {
            updateState(it.episode, State.DOWNLOADED)
        }

        subscribe<Event.FileStored>(eventBus) {
            updateState(it.episode, State.STORED)
        }
    }

    override fun stop() {
        unsubscribe<Event.FileStored>(eventBus)
        unsubscribe<Event.DownloadFinished>(eventBus)
        unsubscribe<Event.DownloadStarted>(eventBus)
        unsubscribe<Event.EpisodeFileFound>(eventBus)
    }

    private fun updateState(file: EpisodeFile, state: State) {

        repository.transaction {
            file.episodes
                .map { EpisodeEntity(it, state) }
                .forEach {
                    update(it)
                    logger.debug { "Set state of episode $it to $state" }
                }
        }

        logger.info { "Set state of episodes to $state: ${file.episodes}" }
    }
}
