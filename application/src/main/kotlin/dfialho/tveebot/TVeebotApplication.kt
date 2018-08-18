package dfialho.tveebot

import dfialho.tveebot.data.ExposedTrackerRepository
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.downloader.libtorrent.threadSafe
import dfialho.tveebot.services.downloader.DownloaderConfig
import dfialho.tveebot.services.tracker.TrackerConfig
import dfialho.tveebot.services.tracker.TrackerService
import dfialho.tveebot.tracker.api.EpisodeRecorder
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import dfialho.tveebot.tracker.lib.ShowRSSProvider
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Files
import javax.sql.DataSource

@SpringBootApplication
class TVeebotApplication {

    @Bean
    fun tvShowProvider(idMapper: TVShowIDMapper): TVShowProvider = ShowRSSProvider(idMapper)

    @Bean
    fun transactionManager(dataSource: DataSource) = SpringTransactionManager(dataSource)

    @Bean
    fun transactionTemplate(manager: SpringTransactionManager) = TransactionTemplate(manager)

    @Bean
    fun trackerEngine(provider: TVShowProvider, recorder: EpisodeRecorder, config: TrackerConfig): TrackerEngine =
        ScheduledTrackerEngine(provider, recorder, config.checkPeriod)

    @Bean
    @Profile("!development")
    fun dataSource(config: DatasourceConfig): DataSource = DriverManagerDataSource().apply {
        setDriverClassName("org.h2.Driver")
        url = "jdbc:h2:${config.savePath.toAbsolutePath()};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    }

    @Bean
    @Profile("development")
    fun dataSourceForDevelopment(): DataSource = DriverManagerDataSource().apply {
        setDriverClassName("org.h2.Driver")
        url = "jdbc:h2:mem:dev;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
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
