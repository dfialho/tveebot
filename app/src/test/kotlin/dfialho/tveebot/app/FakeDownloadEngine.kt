package dfialho.tveebot.app

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.downloader.api.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class FakeDownloadEngine(private val downloadsDirectory: Path) : DownloadEngine {

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

    override fun stop() {
        downloads.clear()
        listeners.clear()
    }

    override fun add(magnetLink: String): DownloadHandle {

        val handle = FakeDownloadHandle(
            DownloadReference(magnetLink),
            isValid = true,
            savePath = downloadsDirectory.resolve(UUID.randomUUID().toString() + ".mkv")
        )

        downloads.putIfAbsent(magnetLink, handle)

        return handle
    }

    fun finish(episodeFile: EpisodeFile) {
        downloads[episodeFile.file.link]?.let { handle ->
            handle.stop()
            Files.createFile(handle.savePath)
            listeners.forEach { it.onFinishedDownload(handle) }
        }
    }

    override fun remove(reference: DownloadReference): Boolean {
        downloads.remove(reference.value)
        return true
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
