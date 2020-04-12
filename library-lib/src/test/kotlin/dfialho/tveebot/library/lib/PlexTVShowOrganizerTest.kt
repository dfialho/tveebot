package dfialho.tveebot.library.lib

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.anyEpisode
import dfialho.tveebot.app.api.models.anyEpisodeFile
import dfialho.tveebot.app.api.models.anyTVShow
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Path
import java.nio.file.Paths

class PlexTVShowOrganizerTest : FunSpec({

    val organizer = PlexTVShowOrganizer()

    fun test(episodeFile: EpisodeFile, expectedLocation: Path) {

        test("$episodeFile -> $expectedLocation") {

            assert(organizer.locationOf(episodeFile))
                .isEqualTo(expectedLocation)
        }
    }

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "Friends"),
                season = 1,
                number = 2,
                title = "The Last One"
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 01", "Friends - s01e02 - The Last One")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "Friends"),
                season = 10,
                number = 2,
                title = "The Last One"
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 10", "Friends - s10e02 - The Last One")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "Friends"),
                season = 1,
                number = 20,
                title = "The Last One"
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 01", "Friends - s01e20 - The Last One")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "Prison Break"),
                season = 1,
                number = 2,
                title = "The Last One"
            )
        ),
        expectedLocation = Paths.get("Prison Break", "Season 01", "Prison Break - s01e02 - The Last One")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "Friends"),
                season = 1,
                number = 2,
                title = ""
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 01", "Friends - s01e02")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "Friends"),
                season = 0,
                number = 0,
                title = "The Last One"
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 00", "Friends - s00e00 - The Last One")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "  Prison Break  "),
                season = 1,
                number = 2,
                title = "  The Last One  "
            )
        ),
        expectedLocation = Paths.get("Prison Break", "Season 01", "Prison Break - s01e02 - The Last One")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                anyTVShow(title = "Friends"),
                season = 1,
                number = 2,
                title = "Mr. Simpson"
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 01", "Friends - s01e02 - Mr. Simpson")
    )

    val tvShow = anyTVShow(title = "Friends")

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                tvShow,
                season = 1,
                number = 20,
                title = "The Last One"
            ),
            anyEpisode(
                tvShow,
                season = 1,
                number = 21,
                title = "The Last One"
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 01", "Friends - s01e20-e21 - The Last One")
    )

    test(
        episodeFile = anyEpisodeFile(
            anyEpisode(
                tvShow,
                season = 1,
                number = 20,
                title = "The Last One (1)"
            ),
            anyEpisode(
                tvShow,
                season = 1,
                number = 21,
                title = "The Last One (2)"
            )
        ),
        expectedLocation = Paths.get("Friends", "Season 01", "Friends - s01e20-e21")
    )

    test("!escape titles") {

    }
})
