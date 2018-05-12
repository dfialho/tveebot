package dfialho.tveebot.downloader.api

import com.google.common.util.concurrent.AbstractIdleService

/**
 * The [DownloadManager] manages the lifecycle of a [DownloadEngine].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class DownloadManager(val engine: DownloadEngine) : AbstractIdleService() {

    override fun startUp() {
        engine.start()
    }

    override fun shutDown() {
        engine.stop()
    }

    fun start() {
        startAsync()
        awaitRunning()
    }

    fun stop() {
        stopAsync()
        awaitTerminated()
    }

}