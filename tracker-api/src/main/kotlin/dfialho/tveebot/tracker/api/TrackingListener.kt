package dfialho.tveebot.tracker.api

import dfialho.tveebot.app.api.models.EpisodeFile

/**
 * A [TrackingListener] listens for new episodes found by a [TrackerEngine]. When a tracking listener is registered
 * with a [TrackerEngine] and the latter finds a new episode, it notifies the listener, providing information about
 * the episode found and the TV show that episode corresponds to.
 */
interface TrackingListener {

    /**
     * Invoked by a [TrackerEngine] when it finds a new episode file.
     *
     * @param file The newly found episode file.
     */
    fun onNewEpisode(file: EpisodeFile)
}
