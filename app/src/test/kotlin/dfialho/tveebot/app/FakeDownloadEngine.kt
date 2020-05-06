package dfialho.tveebot.app

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.downloader.api.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class FakeDownloadEngine(private val downloadsDirectory: Path) : DownloadEngine {

    private val downloads = mutableMapOf<String, Download>()
    private val listeners = mutableListOf<DownloadListener>()

    override fun start() {}

    override fun stop() {
        downloads.clear()
        listeners.clear()
    }

    override fun add(magnetLink: String): Download {

        val download = Download(
            DownloadReference(magnetLink),
            name = magnetLink,
            savePath = downloadsDirectory.resolve(UUID.randomUUID().toString() + ".mkv"),
            status = DownloadStatus(
                DownloadState.DOWNLOADING,
                progress = 0.0f,
                rate = 0
            )
        )

        downloads.putIfAbsent(magnetLink, download)

        return download
    }

    fun finish(episodeFile: EpisodeFile) {
        downloads[episodeFile.file.link]?.let { download ->
            Files.createFile(download.savePath)
            listeners.forEach { it.onFinishedDownload(download) }
        }
    }

    override fun remove(reference: DownloadReference) {
        downloads.remove(reference.value)
    }

    override fun getDownloads(): List<Download> {
        return downloads.values.toList()
    }

    override fun addListener(listener: DownloadListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: DownloadListener) {
        listeners.remove(listener)
    }
}
