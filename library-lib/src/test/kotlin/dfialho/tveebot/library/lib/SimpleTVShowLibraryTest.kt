package dfialho.tveebot.library.lib

import com.nhaarman.mockito_kotlin.mock
import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowUsher
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Paths

object SimpleTVShowLibraryTest : Spek({

    val libraryDirectory = Paths.get("library")

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

        describe("Get location for $episode") {
            val mockedUsher = mock<TVShowUsher>()
            val library: TVShowLibrary = SimpleTVShowLibrary(libraryDirectory, mockedUsher)

            it("should return $expectedPath") {
                library.getLocationOf(episode) shouldEqual expectedPath
            }
        }
    }
})