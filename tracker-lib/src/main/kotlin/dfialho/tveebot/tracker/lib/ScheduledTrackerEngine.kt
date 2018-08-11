package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository

class ScheduledTrackerEngine(
    override val provider: TVShowProvider,
    private val repository: TrackerRepository
) : TrackerEngine {

    override fun start() {

    }

    override fun stop() {

    }

    override fun add(tvShow: TVShow) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(tvShow: TVShow) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeTVShow(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}