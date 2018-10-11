package dfialho.tveebot.data

import dfialho.tveebot.testing.anyTVShow
import dfialho.tveebot.testing.anyTVShowEpisodeFile
import dfialho.tveebot.testing.newRepository
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tvShowEntityOf
import dfialho.tveebot.utils.failed
import dfialho.tveebot.utils.succeeded
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * Tests for the [EpisodeLedgerRepository].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
object EpisodeLedgerRepositoryTest : Spek({

    fun newEpisodeLedger(vararg tvShows: TVShow): EpisodeLedger {

        val repository = newRepository().apply {
            for (tvShow in tvShows) {
                put(tvShowEntityOf(tvShow))
            }
        }

        return EpisodeLedgerRepository(repository)
    }

    describe("Adding different episodes to ledger") {
        val trackedTVShow = anyTVShow()
        val ledger = newEpisodeLedger(trackedTVShow)
        val episodeCount = 5

        repeat(episodeCount) {
            ledger.appendOrUpdate(anyTVShowEpisodeFile(trackedTVShow))
        }

        it("should include all episodes") {
            ledger.size shouldEqualTo episodeCount
        }
    }

    describe("Adding two episodes") {
        val tvShow1 = anyTVShow()
        val tvShow2 = anyTVShow()
        val baseEpisode = anyTVShowEpisodeFile(tvShow1, VideoQuality.HD)

        listOf(
            "qualities" to baseEpisode.copy(quality = VideoQuality.FULL_HD),
            "tv show IDs" to baseEpisode.copy(tvShowID = tvShow2.id, tvShowTitle = tvShow2.title),
            "seasons" to baseEpisode.copy(season = baseEpisode.season + 1),
            "numbers" to baseEpisode.copy(number = baseEpisode.number + 1)
        ).forEach { (propertyName, secondEpisode) ->

            describe("with different $propertyName") {
                val ledger = newEpisodeLedger(tvShow1, tvShow2)

                it("should succeed adding the first episode") {
                    ledger.appendOrUpdate(baseEpisode).succeeded.shouldBeTrue()
                }

                it("should succeed adding the second episode") {
                    ledger.appendOrUpdate(secondEpisode).succeeded.shouldBeTrue()
                }

                it("should include both episodes") {
                    ledger.size shouldEqualTo 2
                }

                it("should contain the first episode") {
                    ledger.shouldContain(baseEpisode)
                }

                it("should contain the second episode") {
                    ledger.shouldContain(secondEpisode)
                }
            }
        }
    }

    describe("An empty ledger") {
        val trackedTVShow = anyTVShow()
        val ledger = newEpisodeLedger(trackedTVShow)

        describe("adding an episode to the ledger") {
            val episode = anyTVShowEpisodeFile(trackedTVShow)

            it("should add episode to ledger") {
                ledger.appendOrUpdate(episode).succeeded.shouldBeTrue()
            }

            it("should contain the new episode") {
                ledger.shouldContain(episode)
            }

            describe("adding the same episode a second time with the same publish date") {

                it("should fail to add episode to ledger") {
                    ledger.appendOrUpdate(episode).failed.shouldBeTrue()
                }

                it("should contain the previous episode") {
                    ledger.shouldContain(episode)
                }

                it("should contain only one episode") {
                    ledger.size shouldEqualTo 1
                }
            }

            describe("adding the same episode a second time with a less recent publish date") {
                val olderEpisode = episode.copy(publishDate = episode.publishDate.minusSeconds(1))

                it("should fail to add episode to ledger") {
                    ledger.appendOrUpdate(olderEpisode).failed.shouldBeTrue()
                }

                it("should contain the previous episode") {
                    ledger.shouldContain(episode)
                }

                it("should contain only one episode") {
                    ledger.size shouldEqualTo 1
                }
            }

            describe("updating the same episode a second time with a more recent publish date") {
                val newerEpisode = episode.copy(
                    publishDate = episode.publishDate.plusSeconds(1),
                    link = "magnet://new-link",
                    episode = episode.toTVShowEpisode().copy(title = "Another Title")
                )

                it("should succeed to update episode") {
                    ledger.appendOrUpdate(newerEpisode).succeeded.shouldBeTrue()
                }

                it("should contain the new episode") {
                    ledger.shouldContain(newerEpisode)
                }

                it("should contain only one episode") {
                    ledger.size shouldEqualTo 1
                }
            }
        }
    }
})

fun EpisodeLedger.shouldContain(episode: TVShowEpisodeFile) {
    this.toList().shouldContain(episode)
}

val EpisodeLedger.size: Int get() = this.toList().size
