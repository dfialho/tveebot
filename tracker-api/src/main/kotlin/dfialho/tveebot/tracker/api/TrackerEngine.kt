package dfialho.tveebot.tracker.api

/**
 * A [TrackerEngine] tracks a set of TV shows marked as "tracked" in the [recorder]. To track these TV shows
 * it uses a [provider], from which it retrieves the episode information about each tracked TV show.
 *
 * The [recorder] also keeps tracking information about each TV show, such as the details about the episode files
 * available for it. A [TrackerEngine] also keeps a set of [TrackingListener]s which are notified every time the
 * tracker obtains a new episode file from the [provider] corresponding to one of the TV shows being tracked.
 *
 * A tracker engine is expected to run on its own thread. For that reason, it provides methods to [start] and [stop]
 * the engine.
 *
 * FIXME
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TrackerEngine {

    /**
     * Provider used to obtain the episode files from each tracked TV show.
     */
    val provider: TVShowProvider

    /**
     * Starts the tracker engine thread. Blocks until the engine is started.
     */
    fun start()

    /**
     * Stops a currently running tracker engine. Calling this method on an already stopped tracke engine should have
     * no side-effects.
     */
    fun stop()

    /**
     * Adds a new [TrackingListener] which will be notified when a new episode file is found by this tracker.
     */
    fun addListener(listener: TrackingListener)

    /**
     * Removes a [TrackingListener]. After calling this method, [listener] will no longer be notified of new events.
     */
    fun removeListener(listener: TrackingListener)
}
