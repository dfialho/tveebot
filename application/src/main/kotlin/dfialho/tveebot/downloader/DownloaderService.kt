package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.api.DownloadEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

@Service
class DownloaderService(val engine: DownloadEngine) : InitializingBean, DisposableBean {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DownloaderService::class.java)
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
