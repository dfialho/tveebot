package dfialho.tveebot.app

import dfialho.tveebot.app.services.ServiceManager
import dfialho.tveebot.app.services.servicesModule
import io.ktor.application.Application
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.ApplicationStopped
import io.ktor.application.log
import io.ktor.server.engine.ApplicationEngineEnvironment
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import kotlin.concurrent.thread


fun Application.app() {

//    val config = loadConfig()
//
//    Database.connect(
//        url = "jdbc:h2:${config.databasePath};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
//        driver = "org.h2.Driver"
//    )

    val kodein = Kodein {
        import(servicesModule)
    }

    val serviceManager by kodein.instance<ServiceManager>()
    serviceManager.start()

    environment.monitor.subscribe(ApplicationStopped) {
        serviceManager.stop()
    }

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        shutdown()
    })
}

fun Application.shutdown() {
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

//    @KtorExperimentalAPI
//    fun Application.loadConfig(): TVeebotConfig {
//
//        try {
//            val config = with(environment.config.config("tveebot")) {
//                TVeebotConfig(
//                    checkPeriod = property("checkPeriod").getString().toLong(),
//                    downloadingDirectory = Paths.get(property("downloadingDirectory").getString()),
//                    libraryDirectory = Paths.get(property("libraryDirectory").getString()),
//                    databasePath = Paths.get(property("databasePath").getString())
//                )
//            }
//
//            log.info("Loaded configuration: $config")
//            return config
//
//        } catch (e: ApplicationConfigurationException) {
//            log.error(e.message.orEmpty(), e)
//            throw e
//        }
//    }
