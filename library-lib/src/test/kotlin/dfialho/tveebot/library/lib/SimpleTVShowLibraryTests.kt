package dfialho.tveebot.library.lib

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowUsher
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import io.mockk.mockk
import org.spekframework.spek2.Spek
import java.nio.file.Paths

/**
 * Tests for the [SimpleTVShowLibrary].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class SimpleTVShowLibraryTests : Spek({

    listOf(
        TVShowEpisode(
            tvShowID = "1",
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 10,
            number = 12
        ) to Paths.get("Show Title", "Season 10", "Show Title - 10x12 - Episode Title"),

        TVShowEpisode(
            tvShowID = "1",
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 1,
            number = 12
        ) to Paths.get("Show Title", "Season 01", "Show Title - 1x12 - Episode Title"),

        TVShowEpisode(
            tvShowID = "1",
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 1,
            number = 2
        ) to Paths.get("Show Title", "Season 01", "Show Title - 1x02 - Episode Title"),

        TVShowEpisode(
            tvShowID = "1",
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 100,
            number = 2
        ) to Paths.get("Show Title", "Season 100", "Show Title - 100x02 - Episode Title"),

        TVShowEpisode(
            tvShowID = "1",
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 1,
            number = 200
        ) to Paths.get("Show Title", "Season 01", "Show Title - 1x200 - Episode Title"),

        TVShowEpisode(
            tvShowID = "1",
            tvShowTitle = "Show Title",
            title = "Episode Title",
            season = 123,
            number = 200
        ) to Paths.get("Show Title", "Season 123", "Show Title - 123x200 - Episode Title")

    ).forEach { (episode, expectedPath) ->

        test("obtaining location from episode='$episode' should return location='$expectedPath'") {
            val libraryDirectory = Paths.get("library")
            val mockedUsher = mockk<TVShowUsher>()
            val library: TVShowLibrary = SimpleTVShowLibrary(libraryDirectory, mockedUsher)

            assert(library.getLocationOf(episode), name = "episode location")
                .isEqualTo(expectedPath)
        }
    }
})