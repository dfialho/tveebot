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
import io.kotlintest.specs.BehaviorSpec

/**
 * Tests for the [EpisodeLedgerRepository].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class EpisodeLedgerRepositoryTests : BehaviorSpec({

    Given("ledger with a single tracked tv show and no episodes") {
        val trackedTVShow = anyTVShow()
        val ledger = newEpisodeLedger(trackedTVShow)
        val episodes = (1..5).map { anyTVShowEpisodeFile(trackedTVShow) }

        When("episodes are for that tv show") {
            episodes.forEach { ledger.appendOrUpdate(it) }

            Then("ledger includes all episodes") {
                assert(ledger).containsAll(episodes)
            }
        }
    }

    Given("ledger with an episode with HD quality") {
        val trackedTVShow = anyTVShow()
        val ledger = newEpisodeLedger(trackedTVShow)
        val originalEpisode = anyTVShowEpisodeFile(trackedTVShow, quality = VideoQuality.HD)
        val newEpisode = originalEpisode.copy(quality = VideoQuality.FULL_HD)

        ledger.appendOrUpdate(originalEpisode)

        When("a second episode with a different quality is added") {
            ledger.appendOrUpdate(newEpisode)

            Then("the ledger includes the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            Then("the ledger includes the new episode") {
                assert(ledger).contains(newEpisode)
            }
        }
    }

    Given("ledger with an episode from season 1") {
        val trackedTVShow = anyTVShow()
        val ledger = newEpisodeLedger(trackedTVShow)
        val originalEpisode = anyTVShowEpisodeFile(trackedTVShow, season = 1)
        val newEpisode = originalEpisode.copy(season = 2)

        ledger.appendOrUpdate(originalEpisode)

        When("a second episode with a different season is added") {
            ledger.appendOrUpdate(newEpisode)

            Then("the ledger includes the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            Then("the ledger includes the new episode") {
                assert(ledger).contains(newEpisode)
            }
        }
    }

    Given("ledger with an episode from season 1 with number 2") {
        val trackedTVShow = anyTVShow()
        val ledger = newEpisodeLedger(trackedTVShow)
        val originalEpisode = anyTVShowEpisodeFile(trackedTVShow, number = 1)
        val newEpisode = originalEpisode.copy(number = 2)

        ledger.appendOrUpdate(originalEpisode)

        When("a second episode with a different number is added") {
            ledger.appendOrUpdate(newEpisode)

            Then("the ledger includes the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            Then("the ledger includes the new episode") {
                assert(ledger).contains(newEpisode)
            }
        }
    }

    Given("ledger with two different tracked tv shows") {
        val tvShow1 = anyTVShow()
        val tvShow2 = anyTVShow()
        val ledger = newEpisodeLedger(tvShow1, tvShow2)

        val baseEpisode = anyEpisodeFile()
        val episode1 = tvShowEpisodeFileOf(tvShow1, baseEpisode)
        val episode2 = tvShowEpisodeFileOf(tvShow2, baseEpisode)

        When("adding an episode from tv show 1") {
            ledger.appendOrUpdate(episode1)

            Then("ledger includes new episode") {
                assert(ledger).contains(episode1)
            }
        }

        When("adding an equal episode but from tv show 2") {
            ledger.appendOrUpdate(episode2)

            Then("ledger includes second episode") {
                assert(ledger).contains(episode2)
            }

            Then("ledger includes first episode") {
                assert(ledger).contains(episode1)
            }
        }
    }

    Given("a ledger with any episode") {
        val trackedTVShow = anyTVShow()
        val ledger = newEpisodeLedger(trackedTVShow)
        val originalEpisode = anyTVShowEpisodeFile(trackedTVShow)

        ledger.appendOrUpdate(originalEpisode)
        val ledgerOriginalSize = ledger.size

        When("trying to add the same episode with the same publish date") {
            val result = ledger.appendOrUpdate(originalEpisode)

            Then("operation fails") {
                assert(result).isEqualTo(Result.Failure)
            }

            Then("ledger contains a single instance of the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            Then("ledger does not contain any new episode") {
                assert(ledger.size, name = "Ledger size").isEqualTo(ledgerOriginalSize)
            }
        }

        When("trying to add the same episode with an older publish date") {
            val olderEpisode = originalEpisode.copy(publishDate = originalEpisode.publishDate.minusSeconds(1))
            val result = ledger.appendOrUpdate(originalEpisode)

            Then("operation fails") {
                assert(result).isEqualTo(Result.Failure)
            }

            Then("ledger contains a single instance of the original episode") {
                assert(ledger).contains(originalEpisode)
            }

            Then("ledger does not contain older episode") {
                assert(ledger).doesNotContain(olderEpisode)
            }
        }

        When("trying to add the same episode with a more recent publish date") {
            val moreRecentEpisode = originalEpisode.copy(publishDate = originalEpisode.publishDate.plusSeconds(1))
            val result = ledger.appendOrUpdate(moreRecentEpisode)

            Then("operation succeeds") {
                assert(result).isEqualTo(Result.Success)
            }

            Then("ledger does not contain the original episode") {
                assert(ledger).doesNotContain(originalEpisode)
            }

            Then("ledger contains the more recent episode") {
                assert(ledger).containsOnly(moreRecentEpisode)
            }
        }
    }
})

/**
 * Creates and returns an episode ledger to test containing the specified [tvShows].
 */
private fun newEpisodeLedger(vararg tvShows: TVShow): EpisodeLedger {

    val repository = newRepository().apply {
        for (tvShow in tvShows) {
            put(tvShowEntityOf(tvShow))
        }
    }

    return EpisodeLedgerRepository(repository)
}
