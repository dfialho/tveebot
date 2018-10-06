package dfialho.tveebot

import dfialho.tveebot.data.EpisodeRecorderRepository
import dfialho.tveebot.data.ExposedTrackerRepository
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.routing.downloader
import dfialho.tveebot.routing.info
import dfialho.tveebot.routing.tracker
import dfialho.tveebot.routing.tveebot
import dfialho.tveebot.services.AlertService
import dfialho.tveebot.services.DownloaderService
import dfialho.tveebot.services.InformationService
import dfialho.tveebot.services.ServiceManager
import dfialho.tveebot.services.TVeebotService
import dfialho.tveebot.services.TrackerService
import dfialho.tveebot.tracker.api.EpisodeRecorder
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.lib.ExposedTVShowIDMapper
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import io.ktor.application.Application
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.ApplicationStopped
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngineEnvironment
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.concurrent.thread


object DbSettings {
    val db: Database by lazy {
        Database.connect("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", driver = "org.h2.Driver")
    }
}

object TVeebotConfig {
    val checkPeriod: Long = 10 // seconds
    val savePath: Path = Paths.get("~/Downloads/tveebot")
}

fun Application.mainModule() {

    val kodein = Kodein {
        bind<Database>() with singleton { DbSettings.db }
        bind<TrackerRepository>() with singleton { ExposedTrackerRepository(instance()) }
        bind<TVShowIDMapper>() with singleton { ExposedTVShowIDMapper(instance()) }
        bind<TVShowProvider>() with singleton { ShowRSSProvider(instance()) }
        bind<EpisodeRecorder>() with singleton { EpisodeRecorderRepository(instance()) }
        bind<DownloadEngine>() with singleton { LibTorrentDownloadEngine(TVeebotConfig.savePath) }
        bind<TrackerEngine>() with singleton { ScheduledTrackerEngine(instance(), instance(), TVeebotConfig.checkPeriod) }
        bind<AlertService>() with singleton { AlertService() }
        bind<DownloaderService>() with singleton { DownloaderService(instance(), instance(), instance()) }
        bind<TrackerService>() with singleton { TrackerService(instance(), instance(), instance(), instance()) }
        bind<InformationService>() with singleton { InformationService(instance()) }
        bind<TVeebotService>() with singleton { TVeebotService(instance(), instance(), instance()) }
        bind<ServiceManager>() with singleton { ServiceManager(instance(), instance(), instance(), instance(), instance()) }
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
