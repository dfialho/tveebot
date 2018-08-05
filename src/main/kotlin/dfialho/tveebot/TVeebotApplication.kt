package dfialho.tveebot

import dfialho.tveebot.downloader.DownloaderConfig
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadManager
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.nio.file.Files
import kotlin.concurrent.thread


@SpringBootApplication
@EnableConfigurationProperties(DownloaderConfig::class)
class TVeebotApplication {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TVeebotApplication::class.java)
    }

    @Bean
    fun downloadManager(config: DownloaderConfig): DownloadManager {

        if (!Files.isDirectory(config.savePath)) {
            throw IllegalArgumentException("The download directory does not exist: ${config.savePath}")
        }

        return DownloadManager(LibTorrentDownloadEngine(config.savePath)).apply {
            logger.debug("Starting download engine")
            start()
            logger.info("Started download engine successfully")

            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                logger.info("Stopping download engine")
                stop()
                logger.info("Stopped download engine successfully")
            })
        }
    }

    @Bean
    fun downloadEngine(downloadManager: DownloadManager): DownloadEngine {
        return downloadManager.engine
    }
}

fun main(args: Array<String>) {
    runApplication<TVeebotApplication>(*args)
}
