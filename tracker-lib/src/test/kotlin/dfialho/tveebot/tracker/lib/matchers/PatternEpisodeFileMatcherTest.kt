package dfialho.tveebot.tracker.lib.matchers

import assertk.assert
import assertk.assertions.*
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.tracker.api.MatchedEpisode
import dfialho.tveebot.tracker.api.MatchedEpisodeFile
import io.kotlintest.specs.FunSpec

class PatternEpisodeFileMatcherTest : FunSpec({

    val matcher = PatternEpisodeFileMatcher(LazyPatternProvider(DefaultPatterns.patterns))

    fun row(title: String, expected: MatchedEpisodeFile) = Pair(title, expected)

    listOf(
        row(
            title = "Friends 1x02 Pilot",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "Pilot",
                        season = 1,
                        number = 2
                    )
                )
            )
        ),
        row(
            title = "Friends 1x12 Pilot",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "Pilot",
                        season = 1,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 Pilot",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "Pilot",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Prison Break 10x12 Pilot",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "Pilot",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "90210 10x12 Pilot",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "Pilot",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The one where it matches",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 Mr. President",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "Mr. President",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12  The   One   Where ",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Prison Break 10x12",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Prison Break 10x12 ",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches 720p",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 720p",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches 1080p",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.FHD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches 480p",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.SD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches 1250p",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches 1250p",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches 720",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches 720",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 720p 10x12 The One Where It Matches",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches REPACK",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches 720p REPACK",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 REPACK",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 720p REPACK",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches PROPER",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 The One Where It Matches 720p PROPER",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 PROPER",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x12 720p PROPER",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 12
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-24",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 24
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-24 The one where it matches",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 24
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-24 720p",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "",
                        season = 10,
                        number = 24
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-24 The one where it matches 720p",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 24
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-24 The one where it matches PROPER",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 24
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-24 The one where it matches 720p PROPER",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 24
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-25 The one where it matches",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.default(),
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 24
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 25
                    )
                )
            )
        ),
        row(
            title = "Friends 10x23-25 The one where it matches 720p PROPER",
            expected = MatchedEpisodeFile(
                videoQuality = VideoQuality.HD,
                episodes = listOf(
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 23
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 24
                    ),
                    MatchedEpisode(
                        title = "The One Where It Matches",
                        season = 10,
                        number = 25
                    )
                )
            )
        )
    ).forEach { (title, expected) ->

        test("match title: $title") {
            assert(matcher.match(title))
                .isEqualTo(expected)
        }
    }

    listOf(
        "",
        "Prison Break",
        "1x02"
    ).forEach { title ->

        test("do not match title: $title") {
            assert(matcher.match(title))
                .isNull()
        }
    }
})
