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
    val information: InformationService,
    val alerting: AlertService,
    val tveebot: TVeebotService
) {
    fun startAll() {
        tveebot.start()
        alerting.start()
        information.start()
        downloader.start()
        tracker.start()
    }

    fun stopAll() {
        tracker.stop()
        downloader.stop()
        information.stop()
        alerting.stop()
        tveebot.stop()
    }
}