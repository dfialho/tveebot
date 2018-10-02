package dfialho.tveebot.services

/**
 * Central point to manage all services used by the application. The service manager is itself a [Service] and it is
 * responsible for making sure all services are started and stopped in the correct order. Furthermore, it provides a central access
 * point to every individual service.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ServiceManager(val tracker: TrackerService, val downloader: DownloaderService) : Service {

    override fun start() {
        downloader.start()
        tracker.start()
    }

    override fun stop() {
        tracker.stop()
        downloader.stop()
    }
}