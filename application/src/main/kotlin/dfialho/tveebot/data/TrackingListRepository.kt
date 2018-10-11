package dfialho.tveebot.data

import dfialho.tveebot.toTVShow
import dfialho.tveebot.tracker.api.TrackingList
import dfialho.tveebot.tracker.api.models.TVShow

class TrackingListRepository(private val repository: TrackerRepository) : TrackingList {

    override fun iterator(): Iterator<TVShow> {
        return repository.findTrackedTVShows()
            .asSequence()
            .map { it.toTVShow() }
            .iterator()
    }
}