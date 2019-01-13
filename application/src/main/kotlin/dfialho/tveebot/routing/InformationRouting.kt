package dfialho.tveebot.routing

import dfialho.tveebot.services.InformationService
import dfialho.tveebot.tracker.api.models.TVShowID
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route


/**
 * Routing for the [InformationService]. Each route described here is handled by the [service]
 * provided to this routing method.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
fun Routing.info(service: InformationService) = route("info") {
    route("tvshows") {

        /**
         * Returns a list containing all TV shows available.
         */
        get {
            call.respond(service.getAllTVShows())
        }

        /**
         * Returns a list containing only the TV shows that are currently being tracked.
         */
        get("tracked") {
            call.respond(service.getTrackedTVShows())
        }

        /**
         * Returns a list containing only the TV shows that are not currently being tracked.
         */
        get("not-tracked") {
            call.respond(service.getNotTrackedTVShows())
        }
    }

    route("episodes") {

        /**
         * Returns a map associating each TV show with all of the episodes found for that
         * particular TV show.
         */
        get {
            call.respond(service.getAllEpisodesByTVShow())
        }

        /**
         * Returns a list containing all of the episodes found for the specified TV show.
         *
         * @param id ID of the TV show to start tracking.
         * @throws NotFoundException if no TV show is found with the specified ID.
         */
        get("tvshow/{id}") {
            val tvShowID = TVShowID(call.requiredParameter("id"))

            call.respond(service.getEpisodesFrom(tvShowID))
        }
    }
}