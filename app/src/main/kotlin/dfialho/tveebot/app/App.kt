package dfialho.tveebot.app

import dfialho.tveebot.app.services.ServiceManager
import dfialho.tveebot.app.services.servicesModule
import io.ktor.application.Application
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.log
import io.ktor.server.engine.ApplicationEngineEnvironment
import org.kodein.di.Kodein
import org.kodein.di.generic.instance


fun Application.app() {

    val kodein = Kodein {
        import(servicesModule)
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
