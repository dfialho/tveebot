package dfialho.tveebot

import dfialho.tveebot.downloader.DownloaderConfig
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadManager
import dfialho.tveebot.downloader.libtorrent.threadSafe
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.tracker.TrackerConfig
import dfialho.tveebot.tracker.api.TrackerEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread


@SpringBootApplication
@EnableConfigurationProperties(DownloaderConfig::class, TrackerConfig::class)
class TVeebotApplication

fun main(args: Array<String>) {
    runApplication<TVeebotApplication>(*args)
}
