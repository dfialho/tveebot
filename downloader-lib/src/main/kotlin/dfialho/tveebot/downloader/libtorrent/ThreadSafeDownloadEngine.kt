package dfialho.tveebot.downloader.libtorrent

import dfialho.tveebot.downloader.api.Download
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadListener
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Download engine tha t ensure thread-safeness for each individual method.
 */
private class ThreadSafeDownloadEngine(private val engine: DownloadEngine) : DownloadEngine {

    /**
     * Lock to manage access to [engine].
     */
    private val lock = ReentrantReadWriteLock()

    override fun start() = lock.write {
        engine.start()
    }

    override fun stop() = lock.write {
        engine.stop()
    }

    override fun add(magnetLink: String): Download = lock.write {
        engine.add(magnetLink)
    }

    override fun getDownloads(): List<Download> = lock.read {
        engine.getDownloads()
    }

    override fun addListener(listener: DownloadListener) = lock.write {
        engine.addListener(listener)
    }

    override fun removeListener(listener: DownloadListener) = lock.write {
        engine.removeListener(listener)
    }
}

/**
 * Returns a thread-safe version of the [DownloadEngine] obtained from [supplier].
 */
fun <T : DownloadEngine> threadSafe(supplier: () -> T): DownloadEngine {
    return ThreadSafeDownloadEngine(supplier())
}
