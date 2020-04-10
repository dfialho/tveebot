package dfialho.tveebot.tracker.lib.matchers

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.tracker.api.MatchedEpisode
import dfialho.tveebot.tracker.api.MatchedEpisodeFile
import io.kotest.core.spec.style.FunSpec

class PatternEpisodeFileMatcherTest : FunSpec({

    val matcher = PatternEpisodeFileMatcher(LazyPatternProvider(DefaultPatterns.patterns))

    fun FunSpec.test(title: String, matched: MatchedEpisodeFile) {

        test("title '${title}' matches to $matched") {
            assert(matcher.match(title))
                .isEqualTo(matched)
        }

    }

    test(
        title = "Friends 1x02 Pilot",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "Pilot",
                    season = 1,
                    number = 2
                )
            )
        )
    )

    test(
        title = "Friends 1x12 Pilot",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "Pilot",
                    season = 1,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 Pilot",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "Pilot",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Prison Break 10x12 Pilot",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "Pilot",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "90210 10x12 Pilot",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "Pilot",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The one where it matches",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 Mr. President",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "Mr. President",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12  The   One   Where ",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Prison Break 10x12",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Prison Break 10x12 ",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches 720p",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.HD,
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 720p",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.HD,
            episodes = listOf(
                MatchedEpisode(
                    title = "",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches 1080p",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.FHD,
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches 480p",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.SD,
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches 1250p",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches 1250p",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches 720",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches 720",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 720p 10x12 The One Where It Matches",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches REPACK",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches 720p REPACK",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.HD,
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 REPACK",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 720p REPACK",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.HD,
            episodes = listOf(
                MatchedEpisode(
                    title = "",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches PROPER",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 The One Where It Matches 720p PROPER",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.HD,
            episodes = listOf(
                MatchedEpisode(
                    title = "The One Where It Matches",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 PROPER",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.default(),
            episodes = listOf(
                MatchedEpisode(
                    title = "",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x12 720p PROPER",
        matched = MatchedEpisodeFile(
            videoQuality = VideoQuality.HD,
            episodes = listOf(
                MatchedEpisode(
                    title = "",
                    season = 10,
                    number = 12
                )
            )
        )
    )

    test(
        title = "Friends 10x23-24",
        matched = MatchedEpisodeFile(
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
    )

    test(
        title = "Friends 10x23-24 The one where it matches",
        matched = MatchedEpisodeFile(
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
    )

    test(
        title = "Friends 10x23-24 720p",
        matched = MatchedEpisodeFile(
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
    )

    test(
        title = "Friends 10x23-24 The one where it matches 720p",
        matched = MatchedEpisodeFile(
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
    )

    test(
        title = "Friends 10x23-24 The one where it matches PROPER",
        matched = MatchedEpisodeFile(
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
    )

    test(
        title = "Friends 10x23-24 The one where it matches 720p PROPER",
        matched = MatchedEpisodeFile(
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
    )

    test(
        title = "Friends 10x23-25 The one where it matches",
        matched = MatchedEpisodeFile(
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
    )

    test(
        title = "Friends 10x23-25 The one where it matches 720p PROPER",
        matched = MatchedEpisodeFile(
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
})
