package dfialho.tveebot.app

import dfialho.tveebot.app.services.DownloaderService
import dfialho.tveebot.app.services.OrganizerService
import dfialho.tveebot.app.services.ServiceManager
import dfialho.tveebot.app.services.TrackerService
import io.ktor.application.Application
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.log
import io.ktor.server.engine.ApplicationEngineEnvironment
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton


fun Application.app() {

    val kodein = Kodein {

        bind<TrackerService>() with singleton { TrackerService(instance(), instance(), instance()) }
        bind<DownloaderService>() with singleton { DownloaderService(instance(), instance()) }
        bind<OrganizerService>() with singleton { OrganizerService(instance(), instance()) }
        bind<ServiceManager>() with singleton {
            ServiceManager(
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
    }

    val serviceManager by kodein.instance<ServiceManager>()
    serviceManager.start()
}

fun Application.appShutdown() {
    log.info("Server is going down")

    environment.let {
        it.monitor.raise(ApplicationStopPreparing, it)

        if (it is ApplicationEngineEnvironment) {
            it.stop()
        } else {
            this.dispose()
        }
    }

    log.info("Server was shutdown successfully")
}
