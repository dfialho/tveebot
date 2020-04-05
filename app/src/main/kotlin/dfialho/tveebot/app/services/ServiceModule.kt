package dfialho.tveebot.app.services

import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.repositories.DatabaseTVeebotRepository
import dfialho.tveebot.app.repositories.EpisodeLedgerRepository
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowOrganizer
import dfialho.tveebot.library.lib.SimpleTVShowLibrary
import dfialho.tveebot.library.lib.SimpleTVShowOrganizer
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.VideoFileParser
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import dfialho.tveebot.tracker.lib.ShowRSSVideoFileParser
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.nio.file.Paths
import java.time.Duration

val servicesModule = Kodein.Module(name = "Services") {
    importOnce(trackerModule)
    importOnce(downloaderModule)
    importOnce(organizerModule)
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

val baseModule = Kodein.Module(name = "Base Module") {
    bind<EventBus>() with singleton { EventBus() }
    bind<TVeebotRepository>() with singleton {
        DatabaseTVeebotRepository(
            Database.connect(
                url = "jdbc:h2:mem:tveebot;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                driver = "org.h2.Driver"
            )
        )
    }
}

val trackerModule = Kodein.Module(name = "Tracker Service") {
    importOnce(baseModule)
    bind<VideoFileParser>() with singleton { ShowRSSVideoFileParser() }
    bind<TVShowProvider>() with singleton { ShowRSSProvider(instance()) }
    bind<EpisodeLedger>() with singleton { EpisodeLedgerRepository(instance()) }
    bind<TrackerEngine>() with singleton { ScheduledTrackerEngine(instance(), instance(), Duration.ofSeconds(1)) }
    bind<TrackerService>() with singleton { TrackerService(instance(), instance(), instance()) }
}

val downloaderModule = Kodein.Module(name = "Downloader Service") {
    importOnce(baseModule)
    bind<DownloadEngine>() with singleton { LibTorrentDownloadEngine(Paths.get("/home/david/Downloads/tveebot/downloads")) }
    bind<DownloaderService>() with singleton { DownloaderService(instance(), instance()) }
}

val organizerModule = Kodein.Module(name = "Organizer Service") {
    importOnce(baseModule)
    bind<TVShowOrganizer>() with singleton { SimpleTVShowOrganizer() }
    bind<TVShowLibrary>() with singleton {
        SimpleTVShowLibrary(
            Paths.get("/home/david/Downloads/tveebot/library"),
            instance()
        )
    }
    bind<OrganizerService>() with singleton { OrganizerService(instance(), instance()) }
}
