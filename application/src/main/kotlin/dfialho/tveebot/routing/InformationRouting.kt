package dfialho.tveebot.routing

import dfialho.tveebot.application.api.ID
import dfialho.tveebot.services.InformationService
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route


/**
 * Routing for the [InformationService]. Each route described here is handled by the [service]
 * provided to this routing method.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
fun Route.info(service: InformationService) = route("info") {
    route("tvshows") {

        /**
         * Returns a list containing all TV shows available.
         */
        get {
            call.respond(service.listAllTVShows())
        }

        /**
         * Returns a list containing only the TV shows that are currently being tracked.
         */
        get("tracked") {
            call.respond(service.listTrackedTVShows())
        }

        /**
         * Returns a list containing only the TV shows that are not currently being tracked.
         */
        get("not-tracked") {
            call.respond(service.listNonTrackedTVShows())
        }
    }

    route("episodes") {

        /**
         * Returns a map associating each TV show with all of the episodes found for that
         * particular TV show.
         */
        get {
            call.respond(service.listEpisodesByTVShow())
        }

        /**
         * Returns a list containing all of the episodes found for the specified TV show.
         *
         * @param id ID of the TV show to start tracking.
         * @throws NotFoundException if no TV show is found with the specified ID.
         */
        get("tvshow/{id}") {
            val tvShowID = ID(call.requiredParameter("id"))

            call.respond(service.getEpisodesFrom(tvShowID))
        }
    }
}