package dfialho.tveebot

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.downloader.libtorrent.threadSafe
import dfialho.tveebot.services.downloader.DownloaderConfig
import dfialho.tveebot.services.tracker.TrackerConfig
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.nio.file.Files
import javax.sql.DataSource

@SpringBootApplication
class TVeebotApplication {

    @Bean
    fun tvShowProvider(idMapper: TVShowIDMapper): TVShowProvider = ShowRSSProvider(idMapper)

    @Bean
    fun transactionManager(dataSource: DataSource) = SpringTransactionManager(dataSource)

    @Bean
    fun trackerEngine(provider: TVShowProvider, repository: TrackerRepository, config: TrackerConfig): TrackerEngine =
        ScheduledTrackerEngine(provider, repository, config.checkPeriod)

    @Bean
    @Profile("!development")
    fun dataSource(config: DatasourceConfig): DataSource = DriverManagerDataSource().apply {
        setDriverClassName("org.h2.Driver")
        url = "jdbc:h2:${config.savePath.toAbsolutePath()};MODE=MYSQL"
    }

    @Bean
    fun downloadEngine(config: DownloaderConfig): DownloadEngine {
        check(Files.isDirectory(config.savePath)) { "Download directory does not exist: ${config.savePath}" }

        return threadSafe { LibTorrentDownloadEngine(config.savePath) }
    }
}

fun main(args: Array<String>) {
    runApplication<TVeebotApplication>(*args)
}
