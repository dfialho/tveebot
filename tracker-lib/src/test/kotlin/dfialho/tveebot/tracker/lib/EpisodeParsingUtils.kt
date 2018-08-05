package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.VideoQuality
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertFailsWith

/**
 * Specs for [parseEpisodeFilename].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
object EpisodeParsingUtils: Spek({

    data class Case(
        val fullTitle: String,
        val expectedEpisode: Episode,
        val expectedQuality: VideoQuality
    )

    describe("An episode full title") {

        listOf(
            Case(
                fullTitle = "Castle 1x02 Crossfire",
                expectedEpisode = Episode(title = "Crossfire", season = 1, number = 2),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Castle 1x23 Crossfire",
                expectedEpisode = Episode(title = "Crossfire", season = 1, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Castle 12x23 Crossfire",
                expectedEpisode = Episode(title = "Crossfire", season = 12, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Castle 1x23 Nice Crossfire",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 1, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Castle 1x23",
                expectedEpisode = Episode(title = "", season = 1, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Prison Break 1x23 Nice Crossfire",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 1, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Prison Break 12x23 Nice Crossfire",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 12, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Castle (2009) 1x23 Nice Crossfire",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 1, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Marvel's Agents of S.H.I.E.L.D 1x23 Fun & Games",
                expectedEpisode = Episode(title = "Fun & Games", season = 1, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "   Castle    12x23  Nice  Crossfire    ",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 12, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Castle 12x23     ",
                expectedEpisode = Episode(title = "", season = 12, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Prison Break 12x23 Nice Crossfire 480p",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 12, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "Prison Break 12x23 Nice Crossfire 720p",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 12, number = 23),
                expectedQuality = VideoQuality.HD
            ),
            Case(
                fullTitle = "Prison Break 12x23 Nice Crossfire 1080p",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 12, number = 23),
                expectedQuality = VideoQuality.FULL_HD
            ),
            Case(
                fullTitle = "Prison Break 12x23 Nice Crossfire 480p 1080p",
                expectedEpisode = Episode(title = "Nice Crossfire 480p", season = 12, number = 23),
                expectedQuality = VideoQuality.FULL_HD
            ),
            Case(
                fullTitle = "Prison Break 12x23 1080p",
                expectedEpisode = Episode(title = "", season = 12, number = 23),
                expectedQuality = VideoQuality.FULL_HD
            ),
            Case(
                fullTitle = "12x23 Nice Crossfire",
                expectedEpisode = Episode(title = "Nice Crossfire", season = 12, number = 23),
                expectedQuality = VideoQuality.SD
            ),
            Case(
                fullTitle = "12x23",
                expectedEpisode = Episode(title = "", season = 12, number = 23),
                expectedQuality = VideoQuality.SD
            )
        ).forEach { (fullTitle, expectedEpisode, expectedQuality) ->

            context(fullTitle) {
                val (episode, quality) = parseEpisodeFilename(fullTitle)

                it("should return episode $expectedEpisode") {
                    episode shouldEqual expectedEpisode
                }

                it("should return video quality $expectedQuality") {
                    quality shouldEqual expectedQuality
                }
            }
        }
    }

    describe("An invalid episode full title") {

        listOf(
            "",
            "Castle Nice Crossfire 1080p",
            "1080p",
            "Castle ax23 Nice Crossfire",
            "Castle 12xa Nice Crossfire",
            "Castle 1234567x23 Nice Crossfire",
            "Castle 12x234567 Nice Crossfire"
        ).forEach { fullTitle ->

            context("invalid->$fullTitle") {
                assertFailsWith(IllegalArgumentException::class) {
                    parseEpisodeFilename(fullTitle)
                }
            }
        }
    }
})

