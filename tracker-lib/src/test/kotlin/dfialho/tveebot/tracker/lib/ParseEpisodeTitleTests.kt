package dfialho.tveebot.tracker.lib

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.tracker.api.models.VideoQuality
import io.kotlintest.specs.FunSpec

/**
 * Tests for utility function [parseEpisodeTitle].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ParseEpisodeTitleTests : FunSpec({

    data class Case(
        val fullTitle: String,
        val expectedEpisode: RawEpisode
    )

    listOf(
        Case(
            fullTitle = "Castle 1x02 Crossfire",
            expectedEpisode = RawEpisode(
                title = "Crossfire",
                season = 1,
                number = 2,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 1x23 Crossfire",
            expectedEpisode = RawEpisode(
                title = "Crossfire",
                season = 1,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 12x23 Crossfire",
            expectedEpisode = RawEpisode(
                title = "Crossfire",
                season = 12,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 1x23 Nice Crossfire",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 1,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 1x23",
            expectedEpisode = RawEpisode(
                title = "",
                season = 1,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 1x23 Nice Crossfire",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 1,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 12,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle (2009) 1x23 Nice Crossfire",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 1,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Marvel's Agents of S.H.I.E.L.D 1x23 Fun & Games",
            expectedEpisode = RawEpisode(
                title = "Fun & Games",
                season = 1,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "   Castle    12x23  Nice  Crossfire    ",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 12,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 12x23     ",
            expectedEpisode = RawEpisode(
                title = "",
                season = 12,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 480p",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 12,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 720p",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 12,
                number = 23,
                quality = VideoQuality.HD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 1080p",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 12,
                number = 23,
                quality = VideoQuality.FULL_HD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 480p 1080p",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire 480p",
                season = 12,
                number = 23,
                quality = VideoQuality.FULL_HD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 1080p",
            expectedEpisode = RawEpisode(
                title = "",
                season = 12,
                number = 23,
                quality = VideoQuality.FULL_HD
            )
        ),
        Case(
            fullTitle = "12x23 Nice Crossfire",
            expectedEpisode = RawEpisode(
                title = "Nice Crossfire",
                season = 12,
                number = 23,
                quality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "12x23",
            expectedEpisode = RawEpisode(
                title = "",
                season = 12,
                number = 23,
                quality = VideoQuality.SD
            )
        )
    ).forEach { (fullTitle, expectedEpisode) ->

        test("parsing title='$fullTitle' should return episode='$expectedEpisode'") {
            val parsedEpisode = parseEpisodeTitle(fullTitle)

            assert(parsedEpisode).isEqualTo(expectedEpisode)
        }
    }
})

