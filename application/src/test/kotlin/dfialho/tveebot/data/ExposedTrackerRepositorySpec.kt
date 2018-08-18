package dfialho.tveebot.data

import dfialho.tveebot.TVeebotApplication
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.services.downloader.EpisodeDownload
import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackedTVShow
import dfialho.tveebot.tracker.api.VideoQuality
import org.amshove.kluent.AnyException
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldContainSome
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotContain
import org.amshove.kluent.shouldNotThrow
import org.amshove.kluent.shouldThrow
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.springframework.transaction.support.TransactionTemplate
import java.lang.Math.random
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Spec for the [ExposedTrackerRepository].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
object ExposedTrackerRepositorySpec : Spek({

    val repository by memoized {
        TransactionalTrackerRepository(
            ExposedTrackerRepository(getTransactionTemplate()).apply {
                afterPropertiesSet()
            }
        )
    }

    given("empty repository") {
        beforeEachTest { repository.clearAll() }

        on("putting a TV show") {
            val tvShow = randomTVShow()
            repository.put(tvShow)

            it("contains the new TV show") {
                repository.findAllTVShows() shouldContain tvShow
            }

            it("TV show is in the non-tracked list") {
                repository.findNotTrackedTVShows() shouldContain tvShow
            }

            it("TV show is not in the tracked list") {
                repository.findTrackedTVShows() shouldNotContain tvShow
            }
        }

        on("putting a tracked TV show") {
            val trackedTVShow = TrackedTVShow(randomTVShow(), quality = VideoQuality.FULL_HD)
            repository.put(trackedTVShow)

            it("contains the new TV show") {
                repository.findAllTVShows() shouldContain trackedTVShow.toTVShow()
            }

            it("TV show is NOT in the non-tracked list") {
                repository.findNotTrackedTVShows() shouldNotContain trackedTVShow.toTVShow()
            }

            it("TV show is in the tracked list") {
                repository.findTrackedTVShows() shouldContain trackedTVShow
            }
        }

        on("putting a collection of new TV shows") {
            val tvShowCollection = randomTVShows(3)
            repository.putAll(tvShowCollection)

            it("none of them is in the tracked list") {
                repository.findTrackedTVShows().shouldBeEmpty()
            }

            it("all of them are in the non-tracked list") {
                repository.findNotTrackedTVShows() shouldContainSame tvShowCollection
            }
        }
    }

    given("repository containing some non-tracked tv shows") {
        val existingTVShow = randomTVShow()
        val originalTVShowList = listOf(
            existingTVShow,
            randomTVShow(),
            randomTVShow(),
            randomTVShow()
        )

        beforeEachTest {
            repository.clearAll()
            repository.putAll(originalTVShowList)
        }

        on("putting a TV show with the same ID as an existing one") {
            val operation = { repository.put(TVShow("new tv show", existingTVShow.id)) }

            it("should throw TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }
        }

        on("putting a tracked TV show with the same ID as a TV show in the non-tracked list") {
            val trackedTVShow = TrackedTVShow(TVShow("new tracked tv show", existingTVShow.id), VideoQuality.FULL_HD)
            val operation = { repository.put(trackedTVShow) }

            it("should throw TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }

            it("should keep the original tv show in the non-tracked list") {
                repository.findNotTrackedTVShows() shouldContain existingTVShow
            }

            it("should not include the new tv show in the repository") {
                repository.findAllTVShows() shouldNotContain trackedTVShow.toTVShow()
            }
        }

        on("putting a tv show with too long title") {
            val operation = { repository.put(TVShow("tv show".repeat(100))) }

            it("should throw IllegalArgumentException") {
                operation shouldThrow IllegalArgumentException::class
            }
        }

        on("putting a collection of if existing tv shows") {
            repository.putAll(listOf(TVShow("new title", existingTVShow.id)))

            it("should keep the original TV shows") {
                repository.findAllTVShows() shouldContainSame originalTVShowList
            }
        }

        val episode = EpisodeFile(
            Episode(title = "my moon", season = 1, number = 1),
            VideoQuality.FULL_HD,
            link = "link",
            publishedDate = randomInstant()
        )

        on("putting an episode from existing TV show") {
            repository.put(existingTVShow.id, episode)

            it("finds the episode associated with that tv show") {
                repository.findEpisodesFrom(existingTVShow.id) shouldContain episode
            }
        }

        on("putting an episode from non-existing TV show") {
            val operation = {
                repository.put(randomTVShow().id, episode)
            }

            it("should throw TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }
        }
    }

    given("repository containing non-tracked and tracked tv shows") {
        val originalVideoQuality = VideoQuality.SD
        val trackedTVShow = TrackedTVShow(TVShow("tracked"), originalVideoQuality)
        val nonTrackedTVShow = TVShow("non-tracked")

        beforeEachTest {
            repository.clearAll()
            repository.put(trackedTVShow)
            repository.put(nonTrackedTVShow)
        }

        on("setting as `tracked` a TV show in the non-tracked list") {
            val newVideoQuality = VideoQuality.FULL_HD
            repository.setTracked(nonTrackedTVShow.id, newVideoQuality)

            it("should no longer contain the tv show in the non-tracked list") {
                repository.findNotTrackedTVShows() shouldNotContain nonTrackedTVShow
            }

            it("should contain the tv show in the tracked list") {
                repository.findTrackedTVShows().map { it.toTVShow() } shouldContain nonTrackedTVShow
            }

            it("should hold the newly specified video quality") {
                repository.findTrackedTVShow(nonTrackedTVShow.id)?.quality shouldEqual newVideoQuality
            }
        }

        on("setting as `tracked` a TV show in the tracked list") {
            val newVideoQuality = VideoQuality.FULL_HD
            repository.setTracked(trackedTVShow.id, newVideoQuality)

            it("should change the tv show's video quality to one specified") {
                repository.findTrackedTVShow(trackedTVShow.id)?.quality shouldEqual newVideoQuality
            }
        }

        on("setting as `not-tracked` an existing tracked TV show") {
            repository.setNotTracked(trackedTVShow.id)

            it("should contain the tv show in the non-tracked list") {
                repository.findNotTrackedTVShows() shouldContain trackedTVShow.toTVShow()
            }

            it("should no longer the tv show in the tracked list") {
                repository.findTrackedTVShows() shouldNotContain trackedTVShow
            }

            it("should not find tracked tv show from the id") {
                repository.findTrackedTVShow(trackedTVShow.id).shouldBeNull()
            }
        }

        on("setting new video quality for tracked TV") {
            val newVideoQuality = VideoQuality.FULL_HD
            repository.setTVShowVideoQuality(trackedTVShow.id, newVideoQuality)

            it("should change the tv show's video quality to one specified") {
                repository.findTrackedTVShow(trackedTVShow.id)?.quality shouldEqual newVideoQuality
            }
        }

        on("setting new video quality for non-tracked TV") {
            val operation = { repository.setTVShowVideoQuality(nonTrackedTVShow.id, VideoQuality.FULL_HD) }

            it("should throw TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }

            it("should not include tv show in the tracked list") {
                repository.findTrackedTVShows().map { it.toTVShow() } shouldNotContain nonTrackedTVShow
            }
        }
    }

    given("repository with some tv shows and episodes") {
        val tvShowList = randomTVShows(4)
        val existingTVShow = tvShowList.first()
        val secondExistingTVShow = tvShowList.last()

        val existingEpisode = episodeWith(season = 1, quality = VideoQuality.SD)
        val existingEpisodeFiles = listOf(
            existingEpisode,
            episodeWith(season = 2),
            episodeWith(season = 3),
            episodeWith(season = 4)
        )

        beforeEachTest {
            repository.clearAll()
            repository.putAll(tvShowList)
            existingEpisodeFiles.forEach { repository.put(existingTVShow.id, it) }
            existingEpisodeFiles.forEach { repository.put(secondExistingTVShow.id, it) }
        }

        on("putting an existing episode") {
            val operation = { repository.put(existingTVShow.id, existingEpisode) }

            it("should throw TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }
        }

        on("putting a file for existing episode but with different quality") {
            val secondEpisodeFile = existingEpisode.copy(quality = VideoQuality.HD)
            repository.put(existingTVShow.id, secondEpisodeFile)

            it("should contain both episode files") {
                repository.findEpisodesFrom(existingTVShow.id) shouldContainSome listOf(existingEpisode, secondEpisodeFile)
            }
        }

        on("putting new episode with a predicate that returns true") {
            val episode = episodeWith(season = 10)
            val returnValue = repository.putOrUpdateIf(existingTVShow.id, episode) { _, _ -> true }

            it("should return true") {
                returnValue shouldEqual true
            }

            it("should contain the episode") {
                repository.findEpisodesFrom(existingTVShow.id) shouldContain episode
            }
        }

        on("putting new episode with a predicate that returns false") {
            val episode = episodeWith(season = 10)
            val returnValue = repository.putOrUpdateIf(existingTVShow.id, episode) { _, _ -> false }

            it("should return true") {
                returnValue shouldEqual true
            }

            it("should contain the episode") {
                repository.findEpisodesFrom(existingTVShow.id) shouldContain episode
            }
        }

        on("putting file of existing episode with a predicate that returns true") {
            val returnValue = repository.putOrUpdateIf(existingTVShow.id, existingEpisode) { _, _ -> true }

            it("should return true") {
                returnValue shouldEqual true
            }

            it("should contain the episode") {
                repository.findEpisodesFrom(existingTVShow.id) shouldContain existingEpisode
            }
        }

        on("putting existing episode with a predicate that returns false") {
            val returnValue = repository.putOrUpdateIf(existingTVShow.id, existingEpisode) { _, _ -> false }

            it("should return false") {
                returnValue shouldEqual false
            }
        }

        on("putting file of existing episode with different properties with a predicate that returns true") {
            val episode = existingEpisode.copy(
                link = "other link",
                publishedDate = existingEpisode.publishedDate.plusSeconds(1)
            )
            val returnValue = repository.putOrUpdateIf(existingTVShow.id, episode) { _, _ -> true }

            it("should return true") {
                returnValue shouldEqual true
            }

            it("should contain the new episode file") {
                repository.findEpisodesFrom(existingTVShow.id) shouldContain episode
            }

            it("should not contain the original episode file") {
                repository.findEpisodesFrom(existingTVShow.id) shouldNotContain existingEpisode
            }
        }

        on("putting file of existing episode with different properties with a predicate that returns false") {
            val episode = existingEpisode.copy(
                link = "other link",
                publishedDate = existingEpisode.publishedDate.plusSeconds(1)
            )
            val returnValue = repository.putOrUpdateIf(existingTVShow.id, episode) { _, _ -> false }

            it("should return false") {
                returnValue shouldEqual false
            }

            it("should not contain the new episode file") {
                repository.findEpisodesFrom(existingTVShow.id) shouldNotContain episode
            }

            it("should contain the original episode file") {
                repository.findEpisodesFrom(existingTVShow.id) shouldContain existingEpisode
            }
        }

        on("removing all episodes from existing tv show") {
            repository.removeEpisodesFrom(existingTVShow.id)

            it("should not contain any episodes for that tv show") {
                repository.findEpisodesFrom(existingTVShow.id).shouldBeEmpty()
            }
        }
    }

    given("repository with tv shows, episodes, and downloads") {
        val existingTVShow = randomTVShow()
        val existingEpisodeWithDownload = episodeWith(season = 1)
        val existingEpisodeWithoutDownload = episodeWith(season = 2)
        val existingDownload = EpisodeDownload(randomReference(), existingTVShow, existingEpisodeWithDownload)

        beforeEachTest {
            repository.clearAll()
            repository.put(existingTVShow)
            repository.put(existingTVShow.id, existingEpisodeWithDownload)
            repository.put(existingTVShow.id, existingEpisodeWithoutDownload)
            repository.put(existingDownload)
        }

        on("putting a download for non-existing episode") {
            val nonExistingEpisode = episodeWith(season = 10)
            val download = EpisodeDownload(randomReference(), existingTVShow, nonExistingEpisode)
            val operation = { repository.put(download) }

            it("should throw a TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }

            it("should not contain download") {
                repository.findAllDownloads() shouldNotContain download
            }

            it("should not find download") {
                repository.findDownload(download.reference).shouldBeNull()
            }
        }

        on("putting a download for existing episode not associated with a download") {
            val download = EpisodeDownload(
                randomReference(),
                existingTVShow,
                existingEpisodeWithoutDownload
            )
            repository.put(download)

            it("should contain the download") {
                repository.findAllDownloads() shouldContain download
            }

            it("should contain the download associated with the tv show") {
                repository.findDownloadsFrom(existingTVShow.id) shouldContain download
            }

            it("should find download") {
                repository.findDownload(download.reference) shouldEqual download
            }
        }

        on("putting a download for episode already associated with a download") {
            val download = EpisodeDownload(
                randomReference(),
                existingTVShow,
                existingEpisodeWithDownload
            )
            repository.put(download)

            it("should contain the download") {
                repository.findAllDownloads() shouldContain download
            }

            it("should contain the download associated with the tv show") {
                repository.findDownloadsFrom(existingTVShow.id) shouldContain download
            }

            it("should find download") {
                repository.findDownload(download.reference) shouldEqual download
            }
        }

        on("putting existing download") {
            val download = EpisodeDownload(
                existingDownload.reference,
                existingTVShow,
                existingEpisodeWithDownload
            )
            val operation = { repository.put(download) }

            it("should throw TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }
        }

        on("putting existing reference for a different episode") {
            val download = EpisodeDownload(
                existingDownload.reference,
                existingTVShow,
                existingEpisodeWithoutDownload
            )
            val operation = { repository.put(download) }

            it("should throw TrackerRepositoryException") {
                operation shouldThrow TrackerRepositoryException::class
            }

            it("should contain the original download") {
                repository.findAllDownloads() shouldContain existingDownload
            }

            it("should not contain the new download") {
                repository.findAllDownloads() shouldNotContain download
            }

            it("should find original download") {
                repository.findDownload(download.reference) shouldEqual existingDownload
            }
        }

        on("removing an existing download") {
            repository.removeDownload(existingDownload.reference)

            it("should not contain the download") {
                repository.findAllDownloads() shouldNotContain existingDownload
            }
        }

        on("removing a non-existing download") {
            val operation = { repository.removeDownload(randomReference()) }

            it("should not throw anything") {
                operation shouldNotThrow AnyException
            }
        }

        on("removing all downloads from an existing tv show") {
            repository.removeAllDownloadsFrom(existingTVShow.id)

            it("should not contain any downloads for that tv show") {
                repository.findDownloadsFrom(existingTVShow.id).shouldBeEmpty()
            }
        }

        on("removing all downloads from a non-existing tv show") {
            val nonExistingTVShow = randomTVShow()
            val operation = { repository.removeAllDownloadsFrom(nonExistingTVShow.id) }

            it("should not throw anything") {
                operation shouldNotThrow AnyException
            }
        }

        on("removing episodes from a tv show") {
            repository.removeEpisodesFrom(existingTVShow.id)

            it("should remove the downloads as well") {
                repository.findDownloadsFrom(existingTVShow.id).shouldBeEmpty()
            }
        }
    }
})

/**
 * Returns a randomly generated [Instant].
 */
private fun randomInstant(): Instant = Instant.ofEpochSecond(random().toInt().toLong())

/**
 * Returns a random string.
 */
private fun randomString(): String = UUID.randomUUID().toString()

/**
 * Generates a list containing [n] random TV shows and returns the list.
 */
private fun randomTVShows(n: Int): List<TVShow> {
    val title = randomString()
    return (1..n).map { TVShow("$title-$it") }
}

/**
 * Generates a single random TV show and returns it.
 */
private fun randomTVShow(): TVShow {
    return randomTVShows(1).first()
}

/**
 * Generates a random [DownloadReference] and returns it.
 */
private fun randomReference() = DownloadReference(randomString())

/**
 * Generates a single random episode file and returns it.
 */
private fun episodeWith(
    season: Int = 1,
    number: Int = 1,
    quality: VideoQuality = VideoQuality.default()
): EpisodeFile {
    val salt = randomString()

    return EpisodeFile(
        Episode("$salt-title", season, number),
        quality,
        "$salt-link",
        randomInstant()
    )
}

private fun getTransactionTemplate(): TransactionTemplate {
    val app = TVeebotApplication()
    return app.transactionTemplate(app.transactionManager(app.dataSourceForDevelopment()))
}

/**
 * Required to have all operations be transactional.
 */
class TransactionalTrackerRepository(private val repository: TrackerRepository) : TrackerRepository {
    override fun put(tvShow: TVShow) = transaction { repository.put(tvShow) }
    override fun put(tvShow: TrackedTVShow) = transaction { repository.put(tvShow) }
    override fun putAll(tvShows: List<TVShow>) = transaction { repository.putAll(tvShows) }
    override fun findTrackedTVShow(tvShowUUID: UUID) = transaction { repository.findTrackedTVShow(tvShowUUID) }
    override fun findAllTVShows() = transaction { repository.findAllTVShows() }
    override fun findTrackedTVShows() = transaction { repository.findTrackedTVShows() }
    override fun findNotTrackedTVShows() = transaction { repository.findNotTrackedTVShows() }
    override fun setTracked(tvShowUUID: UUID, quality: VideoQuality) = transaction { repository.setTracked(tvShowUUID, quality) }
    override fun setNotTracked(tvShowUUID: UUID) = transaction { repository.setNotTracked(tvShowUUID) }
    override fun setTVShowVideoQuality(tvShowUUID: UUID, videoQuality: VideoQuality) = transaction { repository.setTVShowVideoQuality(tvShowUUID, videoQuality) }
    override fun put(tvShowUUID: UUID, episode: EpisodeFile) = transaction { repository.put(tvShowUUID, episode) }
    override fun putOrUpdateIf(tvShowUUID: UUID, episode: EpisodeFile, predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean) = transaction { repository.putOrUpdateIf(tvShowUUID, episode, predicate) }
    override fun findEpisodesFrom(tvShowUUID: UUID) = transaction { repository.findEpisodesFrom(tvShowUUID) }
    override fun removeEpisodesFrom(tvShowUUID: UUID) = transaction { repository.removeEpisodesFrom(tvShowUUID) }
    override fun put(download: EpisodeDownload) = transaction { repository.put(download) }
    override fun findDownload(reference: DownloadReference) = transaction { repository.findDownload(reference) }
    override fun findAllDownloads() = transaction { repository.findAllDownloads() }
    override fun findDownloadsFrom(tvShowUUID: UUID) = transaction { repository.findDownloadsFrom(tvShowUUID) }
    override fun removeDownload(reference: DownloadReference) = transaction { repository.removeDownload(reference) }
    override fun removeAllDownloadsFrom(tvShowUUID: UUID) = transaction { repository.removeAllDownloadsFrom(tvShowUUID) }
    override fun clearAll() = transaction { repository.clearAll() }
}
