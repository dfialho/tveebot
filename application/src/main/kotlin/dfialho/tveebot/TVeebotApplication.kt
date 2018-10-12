package dfialho.tveebot

import dfialho.tveebot.data.EpisodeLedgerRepository
import dfialho.tveebot.data.ExposedTrackerRepository
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.data.TrackingListRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowUsher
import dfialho.tveebot.library.lib.SimpleTVShowLibrary
import dfialho.tveebot.library.lib.SimpleTVShowUsher
import dfialho.tveebot.routing.downloader
import dfialho.tveebot.routing.info
import dfialho.tveebot.routing.tracker
import dfialho.tveebot.routing.tveebot
import dfialho.tveebot.services.AlertService
import dfialho.tveebot.services.DownloaderService
import dfialho.tveebot.services.InformationService
import dfialho.tveebot.services.OrganizerService
import dfialho.tveebot.services.ServiceManager
import dfialho.tveebot.services.TVeebotService
import dfialho.tveebot.services.TrackerService
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingList
import dfialho.tveebot.tracker.lib.DirectTVShowIDMapper
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import io.ktor.application.Application
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.ApplicationStopped
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.config.ApplicationConfigurationException
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngineEnvironment
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import kotlin.concurrent.thread


fun Application.mainModule() {
    val config = loadConfig()

    val kodein = Kodein {
        bind<Database>() with singleton {
            Database.connect(
                url = "jdbc:h2:mem:${config.downloadingDirectory};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                driver = "org.h2.Driver"
            )
        }
        bind<TrackerRepository>() with singleton { ExposedTrackerRepository(instance()) }

        bind<TVShowIDMapper>() with singleton { DirectTVShowIDMapper() }
        bind<TVShowProvider>() with singleton { ShowRSSProvider(instance()) }
        bind<EpisodeLedger>() with singleton { EpisodeLedgerRepository(instance()) }
        bind<TrackingList>() with singleton { TrackingListRepository(instance()) }

        bind<TVShowUsher>() with singleton { SimpleTVShowUsher() }
        bind<TVShowLibrary>() with singleton { SimpleTVShowLibrary(config.libraryDirectory, instance()) }

        bind<TrackerEngine>() with singleton { ScheduledTrackerEngine(instance(), instance(), instance(), config.checkPeriod) }
        bind<DownloadEngine>() with singleton { LibTorrentDownloadEngine(config.downloadingDirectory) }

        bind<AlertService>() with singleton { AlertService() }
        bind<TrackerService>() with singleton { TrackerService(instance(), instance(), instance(), instance()) }
        bind<DownloaderService>() with singleton { DownloaderService(instance(), instance(), instance()) }
        bind<OrganizerService>() with singleton { OrganizerService(instance()) }
        bind<InformationService>() with singleton { InformationService(instance()) }
        bind<TVeebotService>() with singleton { TVeebotService(instance(), instance(), instance(), instance()) }

        bind<ServiceManager>() with singleton {
            ServiceManager(instance(), instance(), instance(), instance(), instance(), instance())
        }
    }

    val serviceManager by kodein.instance<ServiceManager>()
    serviceManager.startAll()

    environment.monitor.subscribe(ApplicationStopped) {
        serviceManager.stopAll()
    }

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        shutdown()
    })

    installModule()
    routingModule(serviceManager)
}

fun Application.loadConfig(): TVeebotConfig {

    try {
        val config = with(environment.config.config("tveebot")) {
            TVeebotConfig(
                checkPeriod = property("checkPeriod").getString().toLong(),
                downloadingDirectory = java.nio.file.Paths.get(property("downloadingDirectory").getString()),
                libraryDirectory = java.nio.file.Paths.get(property("libraryDirectory").getString()),
                databasePath = java.nio.file.Paths.get(property("databasePath").getString())
            )
        }

        log.info("Loaded configuration: $config")
        return config

    } catch (e: ApplicationConfigurationException) {
        log.error(e.message.orEmpty(), e)
        throw e
    }
}

fun Application.installModule() {

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
}

fun Application.routingModule(serviceManager: ServiceManager) {
    routing {
        tracker(serviceManager.tracker)
        downloader(serviceManager.downloader)
        info(serviceManager.information)
        tveebot(serviceManager.tveebot)
    }
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
