package dfialho.tveebot.tracker.api

import dfialho.tveebot.tracker.api.models.TVShow

/**
 * List containing the TV show being tracked.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TrackingList {
    operator fun iterator(): Iterator<TVShow>
}