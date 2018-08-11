package dfialho.tveebot.tracker.api

interface TrackerEngine {

    val repository: TrackerRepository

    val provider: TVShowProvider

    fun start()

    fun stop()
}
