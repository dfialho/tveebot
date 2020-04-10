package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.fire
import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import io.kotest.core.spec.style.BehaviorSpec
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

@Suppress("BlockingMethodInNonBlockingContext")
class DownloaderServiceTest : BehaviorSpec({

    Given("a tracked tv show") {

        val engine = FakeDownloadEngine()
        val services = services(engine)
        val repository by services.instance<TVeebotRepository>()
        startedService<DownloaderService>(services)

        val trackedTVShow = anyTVShow()
        val newEpisodeFile = anyEpisodeFile(tvShow = trackedTVShow)
        repository.upsert(TVShowEntity(trackedTVShow, tracked = true))

        When("a new episode file is found") {
            val recorder = recordEvents<Event.DownloadStarted>(services)
            repository.insert(newEpisodeFile)
            val eventBus by services.instance<EventBus>()
            fire(eventBus, Event.EpisodeFileFound(newEpisodeFile))

            Then("it starts being downloaded") {
                assert(recorder.waitForEvent()?.episode)
                    .isEqualTo(newEpisodeFile)
            }

            Then("the state of the corresponding episode becomes ${State.DOWNLOADING}") {
                val episode = repository.findEpisode(newEpisodeFile.episodes[0].id)
                assert(episode?.state)
                    .isEqualTo(State.DOWNLOADING)
            }
        }

        When("the episode has finished downloading") {
            val recorder = recordEvents<Event.DownloadFinished>(services)
            engine.finish(newEpisodeFile)

            Then("it fires a finished event") {
                assert(recorder.waitForEvent()?.episode)
                    .isEqualTo(newEpisodeFile)
            }

            Then("the state of the corresponding episode becomes ${State.DOWNLOADED}") {
                val episode = repository.findEpisode(newEpisodeFile.episodes[0].id)
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
        startedService<DownloaderService>(services)

        val trackedTVShow = anyTVShow()
        repository.upsert(TVShowEntity(trackedTVShow, tracked = true, videoQuality = VideoQuality.FHD))

        val recorder = recordEvents<Event.DownloadStarted>(services)

        When("a new episode becomes available with video quality ${VideoQuality.SD}") {
            val episodeFile = anyEpisodeFile(
                tvShow = trackedTVShow,
                file = anyVideoFile(quality = VideoQuality.SD)
            )
            fire(eventBus, Event.EpisodeFileFound(episodeFile))

            Then("the episode is not downloaded") {
                assert(recorder.waitForEvent()?.episode)
                    .isNull()
            }
        }

        When("a new episode becomes available with video quality ${VideoQuality.FHD}") {
            val episodeFile = anyEpisodeFile(
                tvShow = trackedTVShow,
                file = anyVideoFile(quality = VideoQuality.FHD)
            )
            fire(eventBus, Event.EpisodeFileFound(episodeFile))

            Then("the episode is downloaded") {
                assert(recorder.waitForEvent()?.episode)
                    .isEqualTo(episodeFile)
            }
        }
    }
})

private fun services(engine: FakeDownloadEngine) = Kodein {
    import(downloaderModule)
    bind<TVeebotRepository>(overrides = true) with instance(newRepository())
    bind<DownloadEngine>(overrides = true) with singleton { engine }
}
