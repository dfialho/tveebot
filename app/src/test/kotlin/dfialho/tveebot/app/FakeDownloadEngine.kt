package dfialho.tveebot.app

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.downloader.api.*
import java.nio.file.Path
import java.nio.file.Paths

class FakeDownloadEngine : DownloadEngine {

    class FakeDownloadHandle(
        override val reference: DownloadReference,
        override var isValid: Boolean,
        override var savePath: Path
    ) : DownloadHandle {

        override fun getStatus(): DownloadStatus {
            notNecessary()
        }

        override fun stop() {
            isValid = false
        }

        override fun pause() {
            notNecessary()
        }

        override fun resume() {
            notNecessary()
        }
    }

    private val downloads = mutableMapOf<String, DownloadHandle>()
    private val listeners = mutableListOf<DownloadListener>()

    override fun start() {}
    override fun stop() {}

    override fun add(torrentFile: Path): DownloadHandle {
        notNecessary()
    }

    override fun add(magnetLink: String): DownloadHandle {

        val handle = FakeDownloadHandle(
            DownloadReference(magnetLink),
            isValid = true,
            savePath = Paths.get("/download/$magnetLink")
        )

        downloads.putIfAbsent(magnetLink, handle)

        return handle
    }

    fun finish(episodeFile: EpisodeFile) {
        downloads[episodeFile.file.link]?.let { handle ->
            handle.stop()
            listeners.forEach { it.onFinishedDownload(handle) }
        }
    }

    override fun remove(reference: DownloadReference): Boolean {
        downloads.remove(reference.value)
        return true
    }

    override fun getHandle(reference: DownloadReference): DownloadHandle? {
        return downloads[reference.value]
    }

    override fun getAllHandles(): List<DownloadHandle> {
        return downloads.values.toList()
    }

    override fun addListener(listener: DownloadListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: DownloadListener) {
        listeners.remove(listener)
    }
}
