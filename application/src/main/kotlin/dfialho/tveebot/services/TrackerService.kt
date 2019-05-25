package dfialho.tveebot.services

import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.services.models.NewEpisodeNotification
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.toTVShow
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingListener
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tvShowEntityOf
import mu.KLogging
import java.util.*

class TrackerService(
    private val engine: TrackerEngine,
    private val provider: TVShowProvider,
    private val repository: TrackerRepository,
    private val alertService: AlertService

) : Service {

    companion object : KLogging()

    private inner class EngineListener : TrackingListener {
        override fun onNewEpisode(episode: TVShowEpisodeFile, tvShowQuality: VideoQuality) {
            logger.info { "Found new episode: ${episode.toPrettyString()}" }
            alertService.raiseAlert(Alerts.NewEpisodeFound, NewEpisodeNotification(episode, tvShowQuality))
        }
    }

    private val engineListener = EngineListener()

    override val name: String = TrackerService::class.simpleName!!

    override fun start() = logStart(logger) {
        repository.putAll(provider.fetchTVShows().map { tvShowEntityOf(it) })
        logger.trace { "Repository: ${repository.findAllTVShows()}" }

        engine.start()
        engine.addListener(engineListener)
    }

    override fun stop() = logStop(logger) {
        engine.stop()
        engine.removeListener(engineListener)
    }

    /**
     * Tells this tracker service to start tracking TV show identified by [tvShowID]. Downloaded episode files for
     * this TV show must be of the specified [videoQuality].
     *
     * @throws IllegalStateException if the TV show with ID [tvShowID] is already being tracked.
     * @throws NoSuchElementException if no TV show is found with id [tvShowID].
     */
    fun trackTVShow(tvShowID: ID, videoQuality: VideoQuality) {
        // FIXME give repository support for transactions with multiple actions
        repository.setTracked(tvShowID, videoQuality)

        repository.findTrackedTVShow(tvShowID)?.let {
            logger.info { "Started tracking TV show: ${it.title}" }
            alertService.raiseAlert(Alerts.StartedTrackingTVShow, it.toTVShow())
        }
    }

    /**
     * Tells this tracker service to stop tracking TV show identified by [tvShowID].
     *
     * @throws IllegalStateException if the TV show with ID [tvShowID] is already being tracked.
     * @throws NoSuchElementException if no TV show is found with id [tvShowID].
     */
    fun untrackTVShow(tvShowID: ID) {
        repository.findTrackedTVShow(tvShowID)?.let {
            repository.setNotTracked(tvShowID)

            logger.info { "Stopped tracking TV show: ${it.title}" }
            alertService.raiseAlert(Alerts.StoppedTrackingTVShow, it.toTVShow())
        }
    }

    /**
     * Forces the tracker to check for new episodes.
     */
    fun check() {
        engine.check()
    }
}