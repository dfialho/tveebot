package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import dfialho.tveebot.downloader.libtorrent.threadSafe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class DownloaderService(config: DownloaderConfig) : InitializingBean, DisposableBean {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DownloaderService::class.java)
    }

    val engine: DownloadEngine by lazy {
        check(Files.isDirectory(config.savePath)) { "Download directory does not exist: ${config.savePath}" }
        threadSafe { LibTorrentDownloadEngine(config.savePath) }
    }

    override fun afterPropertiesSet() {
        logger.debug("Starting downloader service")
        engine.start()
        logger.info("Started downloader service successfully")
    }

    override fun destroy() {
        logger.debug("Stopping downloader service")
        engine.stop()
        logger.info("Stopped downloader service successfully")
    }
}
