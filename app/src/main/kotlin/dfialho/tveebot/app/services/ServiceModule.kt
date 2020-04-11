package dfialho.tveebot.app.services

import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.repositories.DatabaseTVeebotRepository
import dfialho.tveebot.app.repositories.EpisodeLedgerRepository
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.downloader.libtorrent.threadSafe
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowOrganizer
import dfialho.tveebot.library.lib.SimpleTVShowLibrary
import dfialho.tveebot.library.lib.SimpleTVShowOrganizer
import dfialho.tveebot.tracker.api.*
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import dfialho.tveebot.tracker.lib.matchers.DefaultPatterns
import dfialho.tveebot.tracker.lib.matchers.LazyPatternProvider
import dfialho.tveebot.tracker.lib.matchers.PatternEpisodeFileMatcher
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
    importOnce(stateModule)
    bind<ServiceManager>() with singleton {
        ServiceManager(
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}

val baseModule = Kodein.Module(name = "Base Module") {
    bind<EventBus>() with singleton { EventBus() }
    bind<TVeebotRepository>() with singleton { DatabaseTVeebotRepository(instance()) }
}

val trackerModule = Kodein.Module(name = "Tracker Service") {
    importOnce(baseModule)
    bind<PatternProvider>() with singleton { LazyPatternProvider(DefaultPatterns.patterns) }
    bind<EpisodeFileMatcher>() with singleton { PatternEpisodeFileMatcher(instance()) }
    bind<TVShowProvider>() with singleton { ShowRSSProvider(instance()) }
    bind<EpisodeLedger>() with singleton { EpisodeLedgerRepository(instance()) }
    bind<TrackerEngine>() with singleton { ScheduledTrackerEngine(instance(), instance(), Duration.ofSeconds(1)) }
    bind<TrackerService>() with singleton { TrackerService(instance(), instance(), instance()) }
}

val downloaderModule = Kodein.Module(name = "Downloader Service") {
    importOnce(baseModule)
    bind<DownloadEngine>() with singleton { threadSafe { LibTorrentDownloadEngine(Paths.get("/home/david/Downloads/tveebot/downloads")) } }
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

val stateModule = Kodein.Module(name = "State Service") {
    importOnce(baseModule)
    bind<StateService>() with singleton { StateService(instance(), instance()) }
}
