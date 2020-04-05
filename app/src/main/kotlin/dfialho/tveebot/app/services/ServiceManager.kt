package dfialho.tveebot.app.services

import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.repositories.TVeebotRepository
import mu.KLogging

/**
 * Central point to manage all services used by the application. The service manager is responsible
 * for making sure all services are started and stopped in the correct order. Furthermore, it
 * provides a central access point to every individual service.
 */
class ServiceManager(
    private val trackerService: TrackerService,
    private val downloaderService: DownloaderService,
    private val organizerService: OrganizerService,
    private val repository: TVeebotRepository,
    private val eventBus: EventBus
) {
    companion object : KLogging()

    private val startOrder = listOf(
        organizerService,
        downloaderService,
        trackerService
    )

    fun start() {
        logger.info { "Starting services..." }
        for (service in startOrder) {
            logger.debug { "Starting ${service.name}..." }
            service.start()
            logger.info { "Started ${service.name} successfully" }
        }
        logger.info { "All services started successfully" }
    }

    fun stop() {
        logger.info { "Stopping services..." }
        for (service in startOrder.reversed()) {
            logger.debug { "Stopping ${service.name}..." }
            service.stop()
            logger.info { "Stopped ${service.name} successfully" }
        }
        logger.info { "All services stopped successfully" }
    }
}
