package dfialho.tveebot.app.services

import dfialho.tveebot.app.api.models.randomId
import dfialho.tveebot.downloader.api.*
import io.kotlintest.specs.FunSpec
import io.mockk.mockk
import java.nio.file.Path
import java.nio.file.Paths

class DownloaderServiceTest : FunSpec({

//    test("after downloading an episode its state should be ${State.DOWNLOADING}") {
//        withRepository { repository ->
//
//            val downloadEngine = FakeDownloadEngine()
//            val service = DownloaderService(downloadEngine, repository)
//            service.start()
//            val episode = anyEpisodeFile()
//
//            service.download(episode)
//
//            assert(repository.findEpisodes(state = State.DOWNLOADING))
//                .contains(episode)
//        }
//    }
//
//    test("after an episode finishes downloading its state should be ${State.DOWNLOADED}") {
//        withDatabase { db ->
//
//            val stateManager = StateManagerRepository(db)
//            val downloadEngine = FakeDownloadEngine()
//            val service = DownloaderService(downloadEngine, stateManager)
//            service.start()
//            val episode = anyEpisodeFile()
//            service.download(episode)
//            downloadEngine.finishDownload(episode.file.link)
//
//            assert(stateManager.get(episode.episode.id))
//                .isEqualTo(State.DOWNLOADED)
//        }
//    }
//
//    test("trying to download an episode being downloaded should succeed") {
//        withDatabase { db ->
//
//            val episode = EpisodeFile(
//                anyEpisode(),
//                VideoFile(link = "link")
//            )
//            val stateManager = StateManagerRepository(db)
//            val downloadEngine = FakeDownloadEngine()
//            val service = DownloaderService(downloadEngine, stateManager)
//            service.start()
//            service.download(episode)
//
//            assert { service.download(episode) }.doesNotThrowAnyException()
//        }
//    }
})

private data class FakeDownloadHandle(
    override val reference: DownloadReference,
    override val isValid: Boolean,
    override val savePath: Path
) : DownloadHandle {

    companion object {
        fun random(): DownloadHandle = FakeDownloadHandle(
            DownloadReference(randomId()),
            isValid = true,
            savePath = Paths.get("/downloads")
        )
    }

    override fun getStatus(): DownloadStatus {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }
}

private class FakeDownloadEngine : DownloadEngine {

    private val listeners = mutableListOf<DownloadListener>()
    private val handles = mutableMapOf<String, DownloadHandle>()

    fun finishDownload(magnetLink: String) {
        for (listener in listeners) {
            listener.onFinishedDownload(handles[magnetLink] ?: throw IllegalStateException("Check a download was triggered"))
        }
    }

    override fun add(magnetLink: String): DownloadHandle {
        val handle = FakeDownloadHandle.random()
        handles[magnetLink] = handle
        return handle
    }

    override fun addListener(listener: DownloadListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: DownloadListener) {
        listeners.remove(listener)
    }

    override fun start() {}
    override fun stop() {}
    override fun add(torrentFile: Path): DownloadHandle = mockk()
    override fun remove(reference: DownloadReference): Boolean = true
    override fun getHandle(reference: DownloadReference): DownloadHandle? = null
    override fun getAllHandles(): List<DownloadHandle> = emptyList()
}
