package dfialho.tveebot.library.lib

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.library.api.TVShowOrganizer
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import dfialho.tveebot.tracker.api.models.TVShowID
import io.kotlintest.specs.FunSpec
import java.nio.file.Paths

/**
 * Tests for the [SimpleTVShowOrganizer].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class SimpleTVShowOrganizerTests : FunSpec({

    listOf(
        TVShowEpisode(
            tvShowID = TVShowID("1"),
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 10,
            number = 12
        ) to Paths.get("Show Title", "Season 10", "Show Title - 10x12 - Episode Title"),

        TVShowEpisode(
            tvShowID = TVShowID("1"),
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 1,
            number = 12
        ) to Paths.get("Show Title", "Season 01", "Show Title - 1x12 - Episode Title"),

        TVShowEpisode(
            tvShowID = TVShowID("1"),
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 1,
            number = 2
        ) to Paths.get("Show Title", "Season 01", "Show Title - 1x02 - Episode Title"),

        TVShowEpisode(
            tvShowID = TVShowID("1"),
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 100,
            number = 2
        ) to Paths.get("Show Title", "Season 100", "Show Title - 100x02 - Episode Title"),

        TVShowEpisode(
            tvShowID = TVShowID("1"),
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 1,
            number = 200
        ) to Paths.get("Show Title", "Season 01", "Show Title - 1x200 - Episode Title"),

        TVShowEpisode(
            tvShowID = TVShowID("1"),
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 123,
            number = 200
        ) to Paths.get("Show Title", "Season 123", "Show Title - 123x200 - Episode Title")

    ).forEach { (episode, expectedPath) ->

        test("obtaining location from '$episode'") {
            val library: TVShowOrganizer = SimpleTVShowOrganizer()

            assert(library.getLocationOf(episode), name = "episode location")
                .isEqualTo(expectedPath)
        }
    }
})