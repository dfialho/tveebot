package dfialho.tveebot.services

/**
 * Central point to manage all services used by the application. The service manager is responsible
 * for making sure all services are started and stopped in the correct order. Furthermore, it
 * provides a central access point to every individual service.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ServiceManager(
    val tracker: TrackerService,
    val downloader: DownloaderService,
    val organizer: OrganizerService,
    val information: InformationService,
    val alerting: AlertService,
    val tveebot: TVeebotService
) {
    fun startAll() {
        alerting.start()
        tveebot.start()
        information.start()
    }

    fun stopAll() {
        information.stop()
        tveebot.stop()
        alerting.stop()
    }
}