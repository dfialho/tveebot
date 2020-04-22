package dfialho.tveebot.app.rest

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post

fun Route.routes(manager: RestManager) {

    post {
        val registration = call.receive<Registration>()
        manager.register(registration)
        call.respond(HttpStatusCode.Created, "Registered")
    }

    delete("{tvShowId}") {
        val tvShowId = call.parameters["tvShowId"] ?: throw IllegalArgumentException("ID parameter is required")
        manager.unregister(tvShowId)
        call.respond(HttpStatusCode.Accepted, "Un-registered")
    }

    get("status") {
        call.respond(manager.getStatus())
    }

    get("status/all") {
        call.respond(manager.getFiles())
    }

    get("downloads") {
        call.respond(manager.getDownloadStatus())
    }
}
