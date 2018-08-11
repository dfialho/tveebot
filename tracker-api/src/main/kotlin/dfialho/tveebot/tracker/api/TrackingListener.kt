package dfialho.tveebot.tracker.api

/**
 * A [TrackingListener] listens for new [EpisodeFile]s found by a [TrackerEngine]. When a tracking listener is
 * registered with a [TrackerEngine] and the latter finds a new [EpisodeFile], it notifies the listener, providing
 * information about the episode file found and the [TVShow] that episode corresponds to.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TrackingListener {

    /**
     * Invoked by a [TrackerEngine] when it finds a new [EpisodeFile].
     *
     * @param tvShow The TV show to which the [episodeFile] corresponds
     * @param episodeFile The newly found episode file
     */
    fun notify(tvShow: TVShow, episodeFile: EpisodeFile)
}