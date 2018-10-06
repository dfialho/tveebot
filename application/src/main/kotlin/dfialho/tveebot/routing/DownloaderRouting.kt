package dfialho.tveebot.routing

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.exceptions.NotFoundException
import dfialho.tveebot.services.DownloaderService
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

/**
 * Routing for the [DownloaderService]. Each route described here is handled by the [service]
 * provided to this routing method.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
fun Routing.downloader(service: DownloaderService) = route("downloader") {

    route("status") {

        /**
         * Returns a list containing the status of each currently active download.
         */
        get {
            call.respond(service.getAllStatus())
        }

        /**
         * Returns the status information about the download identified by the given reference.
         *
         * @param reference Reference of the download to obtain the status for.
         * @throws NotFoundException if no download is found with the specified reference.
         */
        get("download/{reference}") {
            val reference = DownloadReference(call.requiredParameter("reference"))

            call.respond(service.getStatus(reference))
        }
    }
}
