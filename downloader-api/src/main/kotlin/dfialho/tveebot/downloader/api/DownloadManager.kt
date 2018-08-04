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

    /**
     * Starts the [DownloadEngine]. It blocks until the [DownloadEngine] is started.
     */
    fun start() {
        startAsync()
        awaitRunning()
    }

    /**
     * Stops then [DownloadEngine]. It blocks until the [DownloadEngine] is stopped.
     */
    fun stop() {
        stopAsync()
        awaitTerminated()
    }

}