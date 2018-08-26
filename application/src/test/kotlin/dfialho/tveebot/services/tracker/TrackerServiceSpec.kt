package dfialho.tveebot.services.tracker

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import dfialho.tveebot.data.emptyTrackerRepository
import dfialho.tveebot.services.downloader.DownloaderService
import dfialho.tveebot.services.downloader.EpisodeDownload
import dfialho.tveebot.testing.randomDownloadReference
import dfialho.tveebot.testing.randomEpisodes
import dfialho.tveebot.testing.randomTVShow
import dfialho.tveebot.testing.randomTVShows
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.VideoQuality
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.*
import java.util.UUID.randomUUID
import kotlin.NoSuchElementException

class TrackerServiceSpec : Spek({

    val provider = mock<TVShowProvider>()
    val trackerEngine = mock<TrackerEngine>()
    val downloaderService = mock<DownloaderService>()
    val trackerRepository by memoized { emptyTrackerRepository() }
    val trackerService by memoized { TrackerService(trackerEngine, provider, trackerRepository, downloaderService) }

    beforeEachTest {
        trackerRepository.clearAll()
        reset(downloaderService)
    }

    given("tracker without any tv shows") {

        on("getting all tv shows") {
            val tvShows = trackerService.getAllTVShows()

            it("should return an empty list") {
                tvShows.shouldBeEmpty()
            }
        }

        on("getting tracked tv shows") {
            val tvShows = trackerService.getTrackedTVShows()

            it("should return an empty list") {
                tvShows.shouldBeEmpty()
            }
        }

        on("getting non-tracked tv shows") {
            val tvShows = trackerService.getNotTrackedTVShows()

            it("should return an empty list") {
                tvShows.shouldBeEmpty()
            }
        }

        on("getting all episodes by TV show") {
            val tvShows = trackerService.getAllEpisodesByTVShow()

            it("should return an empty map") {
                tvShows.shouldBeEmpty()
            }
        }

        on("getting episodes from a non-existing tv show") {
            val operation = { trackerService.getEpisodesFrom(randomUUID()) }

            it("should throw NoSuchElementException") {
                operation shouldThrow NoSuchElementException::class
            }
        }
    }

    given("tracker with some tv shows without episodes") {
        val trackedTVShows = randomTVShows(3, tracked = true)
        val nonTrackedTVShows = randomTVShows(3, tracked = false)
        val allTVShows = trackedTVShows + nonTrackedTVShows

        beforeEachTest {
            trackerRepository.putAll(allTVShows)
        }

        on("getting all tv shows") {
            val tvShows = trackerService.getAllTVShows()

            it("should return all tv shows") {
                tvShows shouldContainSame allTVShows
            }
        }

        on("getting tracked tv shows") {
            val tvShows = trackerService.getTrackedTVShows()

            it("should return only tracked tv shows") {
                tvShows shouldContainSame trackedTVShows
            }
        }

        on("getting non-tracked tv shows") {
            val tvShows = trackerService.getNotTrackedTVShows()

            it("should return only non-tracked tv shows") {
                tvShows shouldContainSame nonTrackedTVShows
            }
        }

        on("getting all episodes by TV show") {
            val tvShows = trackerService.getAllEpisodesByTVShow()

            it("should return an empty map") {
                tvShows.shouldBeEmpty()
            }
        }

        on("getting episodes from a existing tv show") {
            val existingTVShow = trackedTVShows.first()
            val episodes = trackerService.getEpisodesFrom(existingTVShow.id)

            it("should return empty list") {
                episodes.shouldBeEmpty()
            }
        }
    }

    given("tracker with some tv shows and episodes") {
        val trackedTVShows = randomTVShows(2, tracked = true)
        val nonTrackedTVShows = randomTVShows(2, tracked = false)
        val allTVShows = trackedTVShows + nonTrackedTVShows

        val firstTVShowEpisodes = randomEpisodes(3, season = 1)
        val secondTVShowEpisodes = randomEpisodes(3, season = 3)
        val firstTVShowWithEpisodes = trackedTVShows.first()
        val secondTVShowWithEpisodes = nonTrackedTVShows.first()

        beforeEachTest {
            trackerRepository.putAll(allTVShows)
            firstTVShowEpisodes.forEach { trackerRepository.put(firstTVShowWithEpisodes.id, it) }
            secondTVShowEpisodes.forEach { trackerRepository.put(secondTVShowWithEpisodes.id, it) }
        }

        on("getting all episodes by TV show") {
            val tvShows: Map<UUID, List<EpisodeFile>> = trackerService.getAllEpisodesByTVShow()

            it("should return a map associating the tv shows and their episodes") {
                tvShows shouldEqual mapOf(
                    firstTVShowWithEpisodes.id to firstTVShowEpisodes,
                    secondTVShowWithEpisodes.id to secondTVShowEpisodes
                )
            }
        }

        on("getting episodes from a non-existing tv show") {
            val operation = { trackerService.getEpisodesFrom(randomUUID()) }

            it("should throw NoSuchElementException") {
                operation shouldThrow NoSuchElementException::class
            }
        }
    }

    given("tracker with some tv shows, episodes, and downloads") {
        val trackedTVShows = randomTVShows(2, tracked = true)
        val nonTrackedTVShows = randomTVShows(2, tracked = false)
        val allTVShows = trackedTVShows + nonTrackedTVShows

        val trackedTVShowDownloadedEpisodes = randomEpisodes(2, season = 1)
        val trackedTVShowDownloadingEpisodes = randomEpisodes(2, season = 2)
        val trackedTVShowEpisodes = trackedTVShowDownloadedEpisodes + trackedTVShowDownloadingEpisodes
        val nonTrackedTVShowEpisodes = randomEpisodes(2, season = 3)
        val trackedTVShowWithEpisodes = trackedTVShows.first()
        val nonTrackedTVShowWithEpisodes = nonTrackedTVShows.first()

        beforeEachTest {
            trackerRepository.putAll(allTVShows)
            nonTrackedTVShowEpisodes.forEach { trackerRepository.put(nonTrackedTVShowWithEpisodes.id, it) }
            trackedTVShowEpisodes.forEach { trackerRepository.put(trackedTVShowWithEpisodes.id, it) }
            trackedTVShowDownloadingEpisodes.forEach {
                trackerRepository.put(EpisodeDownload(randomDownloadReference(), trackedTVShowWithEpisodes, it))
            }
        }

        given("tracked tv show with some downloaded and downloading episodes") {
            val trackedTVShow = randomTVShow(tracked = true)
            val downloadedEpisodes = randomEpisodes(2, season = 1)
            val downloadingEpisodes = randomEpisodes(2, season = 2)

            beforeEachTest {
                trackerRepository.put(trackedTVShow)
                downloadedEpisodes.forEach { trackerRepository.put(trackedTVShow.id, it) }
                downloadingEpisodes.forEach {
                    trackerRepository.put(trackedTVShow.id, it)
                    trackerRepository.put(EpisodeDownload(randomDownloadReference(), trackedTVShow, it))
                }
            }

            on("starting to track it") {
                val operator = { trackerService.trackTVShow(trackedTVShow.id, trackedTVShow.quality) }

                it("should throw an IllegalStateException") {
                    operator shouldThrow IllegalStateException::class
                }

                it("should not call the downloader") {
                    verify(downloaderService, never()).download(any(), any())
                }
            }

            on("stopping to track the tv show") {
                trackerService.untrackTVShow(trackedTVShow.id)

                it("should include the tv show in non-tracked list") {
                    trackerService.getNotTrackedTVShows() shouldContain trackedTVShow
                }

                it("should remove every episode from that tv show currently being downloaded") {
                    verify(downloaderService).removeAllFrom(trackedTVShow.id)
                }

                it("should not remove downloads from other tv shows") {
                    // It was only invoked that one time
                    verify(downloaderService).removeAllFrom(any())
                }

                it("should keep every record of episode files from that tv show") {
                    trackerService.getEpisodesFrom(trackedTVShow.id) shouldContainSame (downloadedEpisodes + downloadingEpisodes)
                }
            }
        }

        given("tracked tv show with some episodes of multiple qualities and downloading some episodes") {
            val trackedTVShow = randomTVShow(tracked = true, quality = VideoQuality.HD)
            val downloadedHDEpisodes = randomEpisodes(2, season = 1,  quality = VideoQuality.HD)
            val downloadingEpisodes = randomEpisodes(2, season = 2,  quality = VideoQuality.HD)
            val newQualityEpisodes = randomEpisodes(2, quality = VideoQuality.SD)
            val otherEpisodes = newQualityEpisodes + downloadedHDEpisodes

            beforeEachTest {
                trackerRepository.put(trackedTVShow)
                otherEpisodes.forEach { trackerRepository.put(trackedTVShow.id, it) }
                downloadingEpisodes.forEach {
                    trackerRepository.put(trackedTVShow.id, it)
                    trackerRepository.put(EpisodeDownload(randomDownloadReference(), trackedTVShow, it))
                }
            }

            on("setting new video quality") {
                val newVideoQuality = VideoQuality.SD
                trackerService.setTVShowVideoQuality(trackedTVShow.id, newVideoQuality)
                val tvShowWithNewQuality = trackedTVShow.copy(quality = newVideoQuality)

                it("should update the TV shows video quality") {
                    trackerService.getAllTVShows() shouldContain tvShowWithNewQuality
                }

                it("should remove downloads") {
                    verify(downloaderService).removeAllFrom(any())
                }

                for (episodeFile in newQualityEpisodes) {
                    it("should invoke downloader with episode file of new quality: $episodeFile") {
                        verify(downloaderService).download(tvShowWithNewQuality, episodeFile)
                    }
                }

                it("should only invoke downloader with episode files of new quality") {
                    verify(downloaderService, times(newQualityEpisodes.size)).download(any(), any())
                }
            }

            on("setting same video quality") {
                trackerService.setTVShowVideoQuality(trackedTVShow.id, trackedTVShow.quality)

                it("should keep the TV show's video quality") {
                    trackerService.getAllTVShows() shouldContain trackedTVShow
                }

                it("should not remove downloads") {
                    verify(downloaderService, never()).removeAllFrom(any())
                }

                it("should not invoke the downloader") {
                    verify(downloaderService, never()).download(any(), any())
                }
            }
        }

        given("non-tracked tv show with recorded episodes") {
            val episodesFilesOfSDQuality = randomEpisodes(n = 3, quality = VideoQuality.SD)
            val episodesFilesOfHDQuality = randomEpisodes(n = 3, quality = VideoQuality.HD)
            val nonTrackedTVShow = randomTVShow(tracked = false, quality = VideoQuality.SD)

            beforeEachTest {
                trackerRepository.put(nonTrackedTVShow)
                episodesFilesOfSDQuality.forEach { trackerRepository.put(nonTrackedTVShow.id, it) }
                episodesFilesOfHDQuality.forEach { trackerRepository.put(nonTrackedTVShow.id, it) }
            }

            on("starting to track that tv show") {
                trackerService.trackTVShow(nonTrackedTVShow.id, nonTrackedTVShow.quality)

                it("should include the tv show in the tracked list") {
                    trackerService.getTrackedTVShows() shouldContain nonTrackedTVShow
                }

                for (episodeFile in episodesFilesOfSDQuality) {
                    it("should start downloading episode file of the configured quality: $episodeFile") {
                        verify(downloaderService).download(nonTrackedTVShow, episodeFile)
                    }
                }

                it("should only start downloading episode file of the configured quality") {
                    verify(downloaderService, times(episodesFilesOfSDQuality.size)).download(any(), any())
                }
            }

            on("stopping to track it") {
                val operator = { trackerService.untrackTVShow(nonTrackedTVShow.id) }

                it("should throw an IllegalStateException") {
                    operator shouldThrow IllegalStateException::class
                }

                it("should not remove downloads from the downloader") {
                    verify(downloaderService, never()).removeAllFrom(any())
                }
            }

            on("setting new video quality") {
                val operation = { trackerService.setTVShowVideoQuality(nonTrackedTVShow.id, VideoQuality.FULL_HD) }

                it("should throw NoSuchElementException") {
                    operation shouldThrow NoSuchElementException::class
                }

                it("should invoke the downloader for each episode of the new quality") {
                    verify(downloaderService, never()).download(any(), any())
                }
            }
        }

        given("non-tracked tv show with no recorded episodes") {
            val nonTrackedTVShow = randomTVShow(tracked = false, quality = VideoQuality.SD)

            beforeEachTest {
                trackerRepository.put(nonTrackedTVShow)
            }

            on("starting to track that tv show") {
                trackerService.trackTVShow(nonTrackedTVShow.id, nonTrackedTVShow.quality)

                it("should not start downloading any episode") {
                    verify(downloaderService, never()).download(any(), any())
                }
            }
        }

        given("non-existing tv show") {
            val tvShowUUID = randomUUID()

            on("starting to track it") {
                val operator = { trackerService.trackTVShow(tvShowUUID, VideoQuality.default()) }

                it("throw a NoSuchElementException") {
                    operator shouldThrow NoSuchElementException::class
                }
            }

            on("stopping to track it") {
                val operator = { trackerService.untrackTVShow(tvShowUUID) }

                it("throw a NoSuchElementException") {
                    operator shouldThrow NoSuchElementException::class
                }
            }

            on("setting video quality") {
                val operator = { trackerService.setTVShowVideoQuality(tvShowUUID, VideoQuality.default()) }

                it("throw a NoSuchElementException") {
                    operator shouldThrow NoSuchElementException::class
                }

                it("should not remove downloads from the downloader") {
                    verify(downloaderService, never()).removeAllFrom(any())
                }
            }
        }
    }
})
