package dfialho.tveebot.routing

import io.ktor.application.ApplicationCall
import io.ktor.request.ApplicationRequest

fun ApplicationCall.requiredParameter(name: String): String {
    return parameters[name] ?: throw IllegalArgumentException("Call is missing required parameter '$name'")
}

fun ApplicationRequest.requiredQueryParameter(name: String): String {
    return queryParameters[name] ?: throw IllegalArgumentException("Request is missing required query parameter '$name'")
}
