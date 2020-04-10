package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.*
import dfialho.tveebot.app.anyEpisodeFile
import dfialho.tveebot.app.anyTVShow
import dfialho.tveebot.app.anyVideoFile
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.fire
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.newRepository
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.downloader.api.*
import io.kotest.core.spec.style.BehaviorSpec
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Suppress("BlockingMethodInNonBlockingContext")
class DownloaderServiceTest : BehaviorSpec({

    val safetyPeriod = 100L

    Given("a started downloader and a tracked TV show") {

        val engine = FakeDownloadEngine()
        val services = services(engine)
        val eventBus by services.instance<EventBus>()
        val repository by services.instance<TVeebotRepository>()
        val service by services.instance<DownloaderService>()
        service.start()

        val trackedTVShow = anyTVShow()
        repository.upsert(TVShowEntity(trackedTVShow, tracked = true))

        val startLatch = CountDownLatch(1)
        subscribe<Event.DownloadStarted>(eventBus) {
            startLatch.countDown()
        }

        val finishLatch = CountDownLatch(1)
        subscribe<Event.DownloadFinished>(eventBus) {
            finishLatch.countDown()
        }

        When("a new episode becomes available") {
            val episodeFile = anyEpisodeFile(tvShow = trackedTVShow)
            repository.insert(episodeFile)
            fire(eventBus, Event.EpisodeFileFound(episodeFile))

            Then("eventually it starts being downloaded") {
                assert(startLatch.await(safetyPeriod, TimeUnit.MILLISECONDS))
                    .isTrue()
            }

            And("the state becomes ${State.DOWNLOADING}") {
                val episode = repository.findEpisode(episodeFile.episodes[0].id)
                assert(episode?.state)
                    .isEqualTo(State.DOWNLOADING)
            }

            And("eventually it finishes being downloaded") {
                engine.finish(episodeFile)
                assert(finishLatch.await(safetyPeriod, TimeUnit.MILLISECONDS))
                    .isTrue()
            }

            And("the state becomes ${State.DOWNLOADED}") {
                val episode = repository.findEpisode(episodeFile.episodes[0].id)
                assert(episode?.state)
                    .isEqualTo(State.DOWNLOADED)
            }
        }
    }

    Given("a started downloader and tv show tracked with quality ${VideoQuality.FHD}") {

        val engine = FakeDownloadEngine()
        val services = services(engine)
        val eventBus by services.instance<EventBus>()
        val repository by services.instance<TVeebotRepository>()
        val service by services.instance<DownloaderService>()
        service.start()

        val trackedTVShow = anyTVShow()
        repository.upsert(TVShowEntity(trackedTVShow, tracked = true, videoQuality = VideoQuality.FHD))

        val downloadedEpisodes = mutableListOf<EpisodeFile>()
        subscribe<Event.DownloadStarted>(eventBus) {
            downloadedEpisodes.add(it.episode)
        }

        When("a new episode becomes available with video quality ${VideoQuality.SD}") {
            val episodeFile = anyEpisodeFile(
                tvShow = trackedTVShow,
                file = anyVideoFile(quality = VideoQuality.SD)
            )
            fire(eventBus, Event.EpisodeFileFound(episodeFile))

            Then("the episode is not downloaded") {
                assert(downloadedEpisodes)
                    .isEmpty()
            }
        }

        When("a new episode becomes available with video quality ${VideoQuality.FHD}") {
            val episodeFile = anyEpisodeFile(
                tvShow = trackedTVShow,
                file = anyVideoFile(quality = VideoQuality.FHD)
            )
            fire(eventBus, Event.EpisodeFileFound(episodeFile))

            Then("the episode is downloaded") {
                assert(downloadedEpisodes)
                    .contains(episodeFile)
            }
        }
    }
})

private fun services(engine: FakeDownloadEngine) = Kodein {
    import(downloaderModule)
    bind<TVeebotRepository>(overrides = true) with instance(newRepository())
    bind<DownloadEngine>(overrides = true) with singleton { engine }
}

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

fun notNecessary(): Nothing {
    throw NotImplementedError("An operation is not implemented because it should be required for the tests")
}
