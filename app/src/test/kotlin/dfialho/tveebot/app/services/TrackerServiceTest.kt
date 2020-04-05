package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertions.*
import dfialho.tveebot.app.anyEpisodeFile
import dfialho.tveebot.app.anyTVShow
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShow
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import dfialho.tveebot.app.newRepository
import dfialho.tveebot.app.repositories.EpisodeLedgerRepository
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.lib.ScheduledTrackerEngine
import io.kotlintest.specs.BehaviorSpec
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Suppress("BlockingMethodInNonBlockingContext")
class TrackerServiceTest : BehaviorSpec({

    Given("a tv show with 2 new episode files") {

        val tvShow = ProvidedTVShow(
            anyTVShow(),
            episodeFiles = listOf(
                anyEpisodeFile(),
                anyEpisodeFile()
            )
        )

        val provider = fakeTVShowProvider(tvShow)

        val repository = newRepository()
        val episodeLedger = EpisodeLedgerRepository(repository)
        val checkPeriod = Duration.ofMillis(10)
        val engine = ScheduledTrackerEngine(provider, episodeLedger, checkPeriod)
        val eventBus = EventBus()
        val service = TrackerService(engine, repository, eventBus)
        service.start()

        val latch = CountDownLatch(tvShow.episodeFiles.size)
        val firedEvents = mutableListOf<Event.EpisodeFileFound>()
        subscribe<Event.EpisodeFileFound>(eventBus) {
            firedEvents.add(it)
            latch.countDown()
        }

        When("it starts being tracked") {
            service.register(tvShow.tvShow.id, VideoQuality.default())

            Then("eventually an ${Event.EpisodeFileFound::class.simpleName} event is fired for each episode file") {
                latch.await(checkPeriod.toMillis() * 2 + 1000, TimeUnit.MILLISECONDS)
                assert(firedEvents)
                    .hasSize(2)
            }
        }
    }
})

data class ProvidedTVShow(val tvShow: TVShow, val episodeFiles: List<EpisodeFile>)

class FakeTVShowProvider(tvShows: List<ProvidedTVShow>) : TVShowProvider {

    private val episodes = tvShows.associateBy({ it.tvShow.id }, { it.episodeFiles })
    private val tvShows = tvShows.associateBy({ it.tvShow.id }, { it.tvShow })

    override fun fetchEpisodes(tvShow: TVShow): List<EpisodeFile> {
        return episodes[tvShow.id] ?: throw IllegalStateException("$tvShow not found")
    }

    override fun fetchTVShow(tvShowId: String): TVShow? {
        return tvShows[tvShowId]
    }
}

fun fakeTVShowProvider(vararg providedTVShows: ProvidedTVShow): TVShowProvider {

    return FakeTVShowProvider(providedTVShows.map { tvShow ->
        tvShow.copy(episodeFiles = tvShow.episodeFiles.map { episodeFile ->
            episodeFile.copy(episodes = episodeFile.episodes.map {
                it.copy(tvShow = tvShow.tvShow)
            })
        })
    })
}
