package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.tracker.api.TrackerEngine
import mu.KLogging

class TrackerService(
    private val engine: TrackerEngine,
    private val eventBus: EventBus
) : Service {

    companion object : KLogging()

    override fun start() {
        logger.debug { "Starting tracker engine..." }
        engine.start()
    }

    override fun stop() {
        logger.debug { "Stopping tracker engine" }
        engine.stop()
    }

    fun register(tvShowId: String, videoQuality: VideoQuality) {
        TODO("Not yet implemented")
    }

    fun unregister(tvShowId: String) {
        TODO("Not yet implemented")
    }
}
