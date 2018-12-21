package dfialho.tveebot.data

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.testing.anyEpisodeFile
import dfialho.tveebot.testing.anyTVShow
import dfialho.tveebot.testing.anyTVShowEpisodeFile
import dfialho.tveebot.testing.newRepository
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tvShowEntityOf
import dfialho.tveebot.tvShowEpisodeFileOf
import dfialho.tveebot.utils.Result
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

/**
 * Tests for the [EpisodeLedgerRepository].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class EpisodeLedgerRepositoryTests : Spek({

    Feature("add episodes to an Episode Ledger") {

        Scenario("adding multiple different episodes from the same tv show") {
            val trackedTVShow = anyTVShow()
            val ledger = newEpisodeLedger(trackedTVShow)
            val episodes = (1..5).map { anyTVShowEpisodeFile(trackedTVShow) }

            Given("ledger with a single tracked tv show and no episodes") {
            }

            When("episodes are for that tv show") {
                episodes.forEach { ledger.appendOrUpdate(it) }
            }

            Then("ledger includes all episodes") {
                assert(ledger).containsAll(episodes)
            }
        }

        Scenario("adding episodes with different qualities") {
            val trackedTVShow = anyTVShow()
            val ledger = newEpisodeLedger(trackedTVShow)
            val originalEpisode = anyTVShowEpisodeFile(trackedTVShow, quality = VideoQuality.HD)
            val newEpisode = originalEpisode.copy(quality = VideoQuality.FULL_HD)

            Given("ledger with a single episode") {
                ledger.appendOrUpdate(originalEpisode)
            }

            When("adding a second episode with a different quality") {
                ledger.appendOrUpdate(newEpisode)
            }

            Then("ledger includes the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            And("the new episode") {
                assert(ledger).contains(newEpisode)
            }
        }

        Scenario("adding episodes with different seasons") {
            val trackedTVShow = anyTVShow()
            val ledger = newEpisodeLedger(trackedTVShow)
            val originalEpisode = anyTVShowEpisodeFile(trackedTVShow, season = 1)
            val newEpisode = originalEpisode.copy(season = 2)

            Given("ledger with a single episode") {
                ledger.appendOrUpdate(originalEpisode)
            }

            When("adding a second episode with a different season") {
                ledger.appendOrUpdate(newEpisode)
            }

            Then("ledger includes the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            And("the new episode") {
                assert(ledger).contains(newEpisode)
            }
        }

        Scenario("adding episodes with different numbers") {
            val trackedTVShow = anyTVShow()
            val ledger = newEpisodeLedger(trackedTVShow)
            val originalEpisode = anyTVShowEpisodeFile(trackedTVShow, number = 1)
            val newEpisode = originalEpisode.copy(number = 2)

            Given("ledger with a single episode") {
                ledger.appendOrUpdate(originalEpisode)
            }

            When("adding a second episode with a different number") {
                ledger.appendOrUpdate(newEpisode)
            }

            Then("ledger includes the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            And("the new episode") {
                assert(ledger).contains(newEpisode)
            }
        }

        Scenario("adding episodes for different tv shows") {
            val tvShow1 = anyTVShow()
            val tvShow2 = anyTVShow()
            val ledger = newEpisodeLedger(tvShow1, tvShow2)

            Given("ledger with two different tracked tv shows") {
            }

            val baseEpisode = anyEpisodeFile()
            val episode1 = tvShowEpisodeFileOf(tvShow1, baseEpisode)
            val episode2 = tvShowEpisodeFileOf(tvShow2, baseEpisode)

            When("adding an episode from tv show 1") {
                ledger.appendOrUpdate(episode1)
            }

            Then("ledger includes episode") {
                assert(ledger).contains(episode1)
            }

            When("adding an equal episode but from tv show 2") {
                ledger.appendOrUpdate(episode2)
            }

            Then("ledger includes second episode") {
                assert(ledger).contains(episode2)
            }

            And("ledger includes first episode") {
                assert(ledger).contains(episode1)
            }
        }
    }

    Feature("updating episodes from an Episode Ledger") {

        Scenario("new episode has the same publish date as previous") {
            val trackedTVShow = anyTVShow()
            val ledger = newEpisodeLedger(trackedTVShow)
            val episode = anyTVShowEpisodeFile(trackedTVShow)

            Given("a ledger with an episode") {
                ledger.appendOrUpdate(episode)
            }

            lateinit var result: Result

            When("trying to add the same episode with the same publish date") {
                result = ledger.appendOrUpdate(episode)
            }

            Then("operation fails") {
                assert(result).isEqualTo(Result.Failure)
            }

            Then("ledger contains only the original episode") {
                assert(ledger).containsOnly(episode)
            }
        }

        Scenario("new episode has a more recent publish date as previous") {
            val trackedTVShow = anyTVShow()
            val ledger = newEpisodeLedger(trackedTVShow)
            val episode = anyTVShowEpisodeFile(trackedTVShow)

            Given("a ledger with an episode") {
                ledger.appendOrUpdate(episode)
            }

            val moreRecentEpisode = episode.copy(publishDate = episode.publishDate.plusSeconds(1))
            lateinit var result: Result

            When("trying to add the same episode with a more recent publish date") {
                result = ledger.appendOrUpdate(moreRecentEpisode)
            }

            Then("operation succeeds") {
                assert(result).isEqualTo(Result.Success)
            }

            Then("ledger contains only the more recent episode") {
                assert(ledger).containsOnly(moreRecentEpisode)
            }
        }

        Scenario("new episode has an older publish date than the previous") {
            val trackedTVShow = anyTVShow()
            val ledger = newEpisodeLedger(trackedTVShow)
            val episode = anyTVShowEpisodeFile(trackedTVShow)

            Given("a ledger with an episode") {
                ledger.appendOrUpdate(episode)
            }

            lateinit var result: Result

            When("trying to add the same episode with the same publish date") {
                result = ledger.appendOrUpdate(episode)
            }

            Then("operation fails") {
                assert(result).isEqualTo(Result.Failure)
            }

            Then("ledger contains only the original episode") {
                assert(ledger).containsOnly(episode)
            }
        }
    }
})

private fun newEpisodeLedger(vararg tvShows: TVShow): EpisodeLedger {

    val repository = newRepository().apply {
        for (tvShow in tvShows) {
            put(tvShowEntityOf(tvShow))
        }
    }

    return EpisodeLedgerRepository(repository)
}
