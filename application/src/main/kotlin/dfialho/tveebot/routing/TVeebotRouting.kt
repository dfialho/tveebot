package dfialho.tveebot.routing

import dfialho.tveebot.services.TVeebotService
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.toVideoQuality
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route


/**
 * Routing for the [TVeebotService]. Each route described here is handled by the [service]
 * provided to this routing method.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
fun Routing.tveebot(service: TVeebotService) = route("tveebot") {

    post("tvshow/{id}/update/quality") {
        val tvShowID = ID(call.requiredParameter("id"))
        val quality = call.request.requiredQueryParameter("quality").toVideoQuality()

        service.setTVShowVideoQuality(tvShowID, quality)
    }
}