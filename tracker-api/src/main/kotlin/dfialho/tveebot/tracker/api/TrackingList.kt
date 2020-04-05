package dfialho.tveebot.tracker.api

import dfialho.tveebot.app.api.models.TVShow

/**
 * List containing the TV show being tracked.
 */
interface TrackingList {
    operator fun iterator(): Iterator<TVShow>
}
