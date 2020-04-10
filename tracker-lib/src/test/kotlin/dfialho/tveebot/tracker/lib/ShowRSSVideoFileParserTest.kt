package dfialho.tveebot.tracker.lib

import assertk.assertions.*
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.tracker.api.MatchedEpisode
import dfialho.tveebot.tracker.api.MatchedEpisodeFile
import io.kotlintest.specs.FunSpec

class ShowRSSVideoFileParserTest : FunSpec({

    data class Case(
        val fullTitle: String,
        val expectedEpisode: MatchedEpisodeFile
    )

    listOf(
        Case(
            fullTitle = "Castle 1x02 Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Crossfire",
                        season = 1,
                        number = 2
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 1x23 Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Crossfire",
                        season = 1,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 12x23 Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Crossfire",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 1x23 Nice Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 1,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 1x23",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 1,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 1x23 Nice Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 1,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle (2009) 1x23 Nice Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 1,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Marvel's Agents of S.H.I.E.L.D 1x23 Fun & Games",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Fun & Games",
                        season = 1,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "   Castle    12x23  Nice  Crossfire    ",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Castle 12x23     ",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 480p",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 720p",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.HD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 1080p",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.FHD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 Nice Crossfire 480p 1080p",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire 480p",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.FHD
            )
        ),
        Case(
            fullTitle = "Prison Break 12x23 1080p",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.FHD
            )
        ),
        Case(
            fullTitle = "12x23 Nice Crossfire",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "Nice Crossfire",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "12x23",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "12x23-24",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 12,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        ),
        Case(
            fullTitle = "12x23-25",
            expectedEpisode = MatchedEpisodeFile(
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 12,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "",
                        season = 12,
                        number = 23
                    )
                ),
                videoQuality = VideoQuality.SD
            )
        )
    ).forEach { (fullTitle, expectedEpisode) ->


        test("parsing title='$fullTitle' should return episode='$expectedEpisode'") {
            val parser = ShowRSSVideoFileParser()

            assertk.assert(parser.parse(fullTitle))
                .isEqualTo(expectedEpisode)
        }
    }
})
