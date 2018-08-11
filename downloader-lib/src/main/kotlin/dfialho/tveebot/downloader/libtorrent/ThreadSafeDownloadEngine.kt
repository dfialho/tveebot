package dfialho.tveebot.downloader.libtorrent

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadHandle
import dfialho.tveebot.downloader.api.DownloadReference
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Download engine that ensure thread-safeness for each individual method.
 *
 * @author David Fialho (dfialho@protonmail.com)
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

    override fun add(torrentFile: Path): DownloadHandle = lock.write {
        engine.add(torrentFile)
    }

    override fun add(magnetLink: String): DownloadHandle = lock.write {
        engine.add(magnetLink)
    }

    override fun remove(reference: DownloadReference) = lock.write {
        engine.remove(reference)
    }

    override fun getHandle(reference: DownloadReference): DownloadHandle? = lock.read {
        engine.getHandle(reference)
    }

    override fun getAllHandles(): List<DownloadHandle> = lock.read {
        engine.getAllHandles()
    }
}

/**
 * Returns a thread-safe version of the [DownloadEngine] obtained from [supplier].
 */
fun <T : DownloadEngine> threadSafe(supplier: () -> T): DownloadEngine {
    return ThreadSafeDownloadEngine(supplier())
}
