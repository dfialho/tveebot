package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.fire
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingListener
import mu.KLogging

class TrackerService(
    private val engine: TrackerEngine,
    private val repository: TVeebotRepository,
    private val eventBus: EventBus
) : Service {

    companion object : KLogging()

    private val engineListener: TrackingListener = object : TrackingListener {
        override fun onNewEpisode(file: EpisodeFile) {
            logger.info { "Found new episode file: $file" }
            fire(eventBus, Event.EpisodeFileFound(file))
        }
    }

    override fun start() {
        logger.debug { "Starting tracker engine..." }
        engine.addListener(engineListener)
        engine.start()
    }

    override fun stop() {
        logger.debug { "Stopping tracker engine..." }
        engine.stop()
        engine.removeListener(engineListener)
    }

    fun register(tvShowId: String, videoQuality: VideoQuality) {

        repository.transaction {
            val tvShow = findTVShow(tvShowId)
                ?: fetchTVShow(tvShowId)
                ?: throw IllegalStateException("No TV show found with id $tvShowId")

            upsert(tvShow.copy(tracked = true, videoQuality = videoQuality))
            engine.register(tvShow.tvShow)
            logger.info { "Started tracking ${tvShow.tvShow} with video quality $videoQuality" }
        }
    }

    fun unregister(tvShowId: String) {

        repository.transaction {
            findTVShow(tvShowId, tracked = true)?.let { tvShow ->
                update(tvShow.copy(tracked = false))
                engine.unregister(tvShow.tvShow.id)
                logger.info { "Stopped tracking ${tvShow.tvShow}" }
            }
        }
    }

    private fun fetchTVShow(tvShowId: String): TVShowEntity? {

        logger.debug { "Fetching TV show with id '$tvShowId' from provider" }
        return engine.provider.fetchTVShow(tvShowId)?.let {
            TVShowEntity(it, tracked = false, videoQuality = VideoQuality.default())
        }
    }
}