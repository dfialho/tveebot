package dfialho.tveebot.app.services

import dfialho.tveebot.app.AppConfig
import dfialho.tveebot.app.components.DownloadCleaner
import dfialho.tveebot.app.components.DownloadScanner
import dfialho.tveebot.app.components.DownloadTracker
import dfialho.tveebot.app.components.FileStash
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.repositories.*
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.downloader.libtorrent.threadSafe
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowOrganizer
import dfialho.tveebot.library.lib.FileSystemTVShowLibrary
import dfialho.tveebot.library.lib.PlexTVShowOrganizer
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

val servicesModule = Kodein.Module(name = "Services") {
    importOnce(trackerModule)
    importOnce(downloaderModule)
    importOnce(libraryModule)
    importOnce(stateModule)
    importOnce(stashModule)
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
    bind<TVeebotRepository>() with singleton { DatabaseTVeebotRepository(instance()) }
}

val trackerModule = Kodein.Module(name = "Tracker Service") {
    importOnce(baseModule)
    bind<PatternProvider>() with singleton { LazyPatternProvider(DefaultPatterns.patterns) }
    bind<EpisodeFileMatcher>() with singleton { PatternEpisodeFileMatcher(instance()) }
    bind<TVShowProvider>() with singleton { ShowRSSProvider(instance()) }
    bind<EpisodeLedger>() with singleton { EpisodeLedgerRepository(instance()) }
    bind<TrackerEngine>() with singleton { ScheduledTrackerEngine(instance(), instance(), instance<AppConfig>().checkPeriod) }
    bind<TrackerService>() with singleton { TrackerService(instance(), instance(), instance()) }
}

val downloaderModule = Kodein.Module(name = "Downloader Service") {
    importOnce(baseModule)
    bind<DownloadEngine>() with singleton { threadSafe { LibTorrentDownloadEngine(instance<AppConfig>().downloadingDirectory) } }
    bind<DownloadTracker>() with singleton { DownloadTrackerRepository(instance(), instance()) }
    bind<DownloadCleaner>() with singleton {
        DownloadCleaner(
            instance()
        )
    }
    bind<DownloadScanner>() with singleton { DownloadScanner() }
    bind<DownloaderService>() with singleton { DownloaderService(instance(), instance(), instance(), instance()) }
}

val libraryModule = Kodein.Module(name = "Library Service") {
    importOnce(baseModule)
    bind<TVShowOrganizer>() with singleton { PlexTVShowOrganizer() }
    bind<TVShowLibrary>() with singleton { FileSystemTVShowLibrary(instance<AppConfig>().libraryDirectory, instance()) }
    bind<LibraryService>() with singleton { LibraryService(instance(), instance()) }
}

val stateModule = Kodein.Module(name = "State Service") {
    importOnce(baseModule)
    bind<StateService>() with singleton { StateService(instance(), instance()) }
}

val stashModule = Kodein.Module(name = "File Stash Service") {
    importOnce(baseModule)
    bind<FileStashRepository>() with singleton { DatabaseFileStashRepository(instance()) }
    bind<FileStash>() with singleton { FileStash(instance<AppConfig>().downloadedDirectory, instance()) }
    bind<FileStashService>() with singleton { FileStashService(instance(), instance(), instance()) }
}
