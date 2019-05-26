package dfialho.tveebot.routing

import dfialho.tveebot.services.TrackerService
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tracker.api.models.toVideoQuality
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

/**
 * Routing for the [TrackerService]. Each route described here is handled by the [service] provided
 * to this routing method.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
fun Route.tracker(service: TrackerService) = route("tracker") {

    /**
     * Requests the tracker to start tracking the TV show identified by the specified ID.
     * It takes an optional query parameter 'quality' which specifies the video quality of the
     * episodes to be downloaded. The options are '480p', '720p', and '1080p'. By default,
     * '480p' is used.
     *
     * @param id ID of the TV show to start tracking.
     * @throws AlreadyExistsException if the TV show is already being tracked.
     * @throws NotFoundException if no TV show is found with the specified ID.
     */
    post("track/{id}") {
        val tvShowID = ID(call.requiredParameter("id"))
        val quality = call.request.queryParameters["quality"]?.toVideoQuality() ?: VideoQuality.default()

        service.trackTVShow(tvShowID, quality)
        call.respondText { "Ok" }
    }


    /**
     * Requests the tacker to stop tracking the TV show identified by the specified ID. Every
     * current episodeFile being downloaded will be stopped. If the TV show corresponding to the
     * given ID is currently not being tracked, then this call will have no effect.
     *
     * @param id ID of the TV show to stop tracking.
     * @throws NotFoundException if no TV show is found with the specified ID.
     */
    post("untrack/{id}") {
        val tvShowID = ID(call.requiredParameter("id"))

        service.untrackTVShow(tvShowID)
        call.respondText { "Ok" }
    }
}
