package dfialho.tveebot.tracker.lib

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import dfialho.tveebot.tracker.api.models.Episode
import dfialho.tveebot.tracker.api.models.VideoQuality
import org.spekframework.spek2.Spek

/**
 * Tests for utility function [parseEpisodeTitle].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ParseEpisodeTitleTests : Spek({

    data class Case(
        val fullTitle: String,
        val expectedEpisode: Episode,
        val expectedQuality: VideoQuality
    )

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

        test("parsing title='$fullTitle' should return episode='$expectedEpisode' with quality='$expectedQuality'") {
            val (episode, quality) = parseEpisodeTitle(fullTitle)

            assertAll {
                assert(episode).isEqualTo(expectedEpisode)
                assert(quality).isEqualTo(expectedQuality)
            }
        }
    }
})

