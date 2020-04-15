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

            // TODO Data Model: It should not be possible to have multiple tv shows in an episode file
            val tvShow = file.episodes[0].tvShow
            val trackedTVShow = repository.findTVShow(tvShow.id, tracked = true)

            if (trackedTVShow == null) {
                logger.info { "Ignoring episode file because the tv show is not tracked: $file" }
                return
            }

            if (file.file.quality != trackedTVShow.videoQuality) {
                logger.info { "Ignoring episode file because tv show is tracking files with quality " +
                    "${trackedTVShow.videoQuality} but the file has quality ${file.file.quality}: $file" }
                return
            }

            fire(eventBus, Event.EpisodeFileFound(file))
        }
    }

    override fun start() {
        logger.debug { "Starting tracker engine..." }
        engine.addListener(engineListener)
        engine.start()
        logger.debug { "Started tracker engine" }
    }

    override fun stop() {
        logger.debug { "Stopping tracker engine..." }
        engine.stop()
        engine.removeListener(engineListener)
        logger.debug { "Stopped tracker engine" }
    }

    fun register(tvShowId: String, videoQuality: VideoQuality) {

        val tvShow = repository.transaction {
            val tvShow = findTVShow(tvShowId)
                ?: fetchTVShow(tvShowId)
                ?: throw IllegalStateException("No TV show found with id $tvShowId")

            upsert(tvShow.copy(tracked = true, videoQuality = videoQuality))
            tvShow
        }

        engine.register(tvShow.tvShow)
        logger.info { "Started tracking ${tvShow.tvShow} with video quality $videoQuality" }
        engine.check()
    }

    fun unregister(tvShowId: String) {

        val tvShow = repository.transaction {
            findTVShow(tvShowId, tracked = true)?.also {
                upsert(it.copy(tracked = false))
            }
        }

        tvShow?.let {
            engine.unregister(tvShow.tvShow.id)
            logger.info { "Stopped tracking ${tvShow.tvShow}" }
        }
    }

    private fun fetchTVShow(tvShowId: String): TVShowEntity? {

        logger.info { "Fetching TV show with id '$tvShowId' from provider" }
        return engine.provider.fetchTVShow(tvShowId)?.let {
            TVShowEntity(it, tracked = false, videoQuality = VideoQuality.default())
        }
    }
}
