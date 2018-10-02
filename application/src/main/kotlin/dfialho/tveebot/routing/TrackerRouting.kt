package dfialho.tveebot.routing

import dfialho.tveebot.exceptions.AlreadyExistsException
import dfialho.tveebot.exceptions.NotFoundException
import dfialho.tveebot.services.TrackerService
import dfialho.tveebot.tracker.api.VideoQuality
import dfialho.tveebot.tracker.api.toVideoQuality
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import java.util.*

/**
 * Routing for the [TrackerService]. Each route described here is handled by the [service] provided
 * to this routing method.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
fun Routing.tracker(service: TrackerService) = route("tracker") {

    route("tvshows") {

        /**
         * Returns a list containing all TV shows available.
         */
        get("all") {
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
        get("all") {
            call.respond(service.getAllEpisodesByTVShow())
        }

        /**
         * Returns a list containing all of the episodes found for the specified TV show.
         *
         * @throws NotFoundException if no TV show is found with the specified ID.
         */
        get("tvshow/{id}") {
            val tvShowID = call.getIDParameter("id")

            call.respond(service.getEpisodesFrom(tvShowID))
        }
    }

    route("tvshow") {

        /**
         * Returns available information about the TV show identified by the specified ID. This does
         * not include the episodes available for the corresponding TV show.
         *
         * @throws NotFoundException if no TV show is found with the specified ID.
         */
        get("{id}") {
            val tvShowID = call.getIDParameter("id")

            TODO("to be implemented")
        }

        /**
         * Requests the tracker to start tracking the TV show identified by the specified ID.
         * It takes an optional query parameter 'quality' which specifies the video quality of the
         * episodes to be downloaded. The options are '480p', '720p', and '1080p'. By default,
         * '480p' is used.
         *
         * @throws AlreadyExistsException if the TV show is already being tracked.
         * @throws NotFoundException if no TV show is found with the specified ID.
         */
        post("{id}") {
            val tvShowID = call.getIDParameter("id")
            val quality = call.request.queryParameters["quality"]?.toVideoQuality() ?: VideoQuality.default()

            service.trackTVShow(tvShowID, quality)
            call.respondText { "Ok" }
        }

        /**
         * Updates the configuration of the TV show identified by the specified ID. At this point,
         * this endpoint only enables specifying a different video quality.
         *
         * If a new video quality is set for a TV show, then every current download will be stopped
         * and previously found episode files matching the new video quality will start to be
         * downloaded.
         *
         * @throws NotFoundException if no TV show is found with the specified ID.
         */
        post("{id}/update") {
            val tvShowID = call.getIDParameter("id")
            val quality = call.request.queryParameters["quality"]?.toVideoQuality() ?: throw IllegalArgumentException("Missing video quality")

            service.setTVShowVideoQuality(tvShowID, quality)
            call.respondText { "Ok" }
        }

        /**
         * Requests the tacker to stop tracking the TV show identified by the specified ID. Every
         * current episode being downloaded will be stopped. If the TV show corresponding to the
         * given ID is currently not being tracked, then this call will have no effect.
         *
         * @throws NotFoundException if no TV show is found with the specified ID.
         */
        delete("{id}") {
            val tvShowID = UUID.fromString(call.parameters["id"])

            service.untrackTVShow(tvShowID)
            call.respondText { "Ok" }
        }
    }
}

private fun ApplicationCall.getIDParameter(name: String): UUID = UUID.fromString(parameters[name])
