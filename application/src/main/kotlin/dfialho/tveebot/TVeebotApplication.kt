package dfialho.tveebot

import dfialho.tveebot.downloader.DownloaderConfig
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.threadSafe
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.tracker.TrackerConfig
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.lib.InMemoryTVShowIDMapper
import dfialho.tveebot.tracker.lib.InMemoryTrackerRepository
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.nio.file.Files


@SpringBootApplication
@EnableConfigurationProperties(DownloaderConfig::class, TrackerConfig::class)
class TVeebotApplication {

    @Bean
    fun tvShowIDMapper(): TVShowIDMapper = InMemoryTVShowIDMapper()

    @Bean
    fun tvShowProvider(idMapper: TVShowIDMapper): TVShowProvider = ShowRSSProvider(idMapper)

    @Bean
    fun trackerRepository(): TrackerRepository = InMemoryTrackerRepository()

    @Bean
    fun downloadEngine(provider: TVShowProvider, repository: TrackerRepository): TrackerEngine =
        ScheduledTrackerEngine(provider, repository)
}

fun main(args: Array<String>) {
    runApplication<TVeebotApplication>(*args)
}
