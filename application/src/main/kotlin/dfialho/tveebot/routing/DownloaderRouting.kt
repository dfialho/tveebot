package dfialho.tveebot.routing

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.services.DownloaderService
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.route

/**
 * Routing for the [DownloaderService]. Each route described here is handled by the [service]
 * provided to this routing method.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
fun Routing.downloader(service: DownloaderService) = route("downloader") {

    /**
     * Returns a list containing the status of each currently active download.
     */
    get("status") {
        call.respond(service.getAllStatus())
    }

    /**
     * Returns the status information about the download identified by the given reference.
     */
    get("status/{reference}") {
        val reference = DownloadReference(call.parameters["reference"] ?: throw IllegalArgumentException())
        call.respond(service.getStatus(reference))
    }

    /**
     * Requests the downloader to stop and remove the download identified by the given reference.
     */
    delete("{reference}") {
        val reference = DownloadReference(call.parameters["reference"] ?: throw IllegalArgumentException())
        call.respond(service.removeDownload(reference))
    }
}
