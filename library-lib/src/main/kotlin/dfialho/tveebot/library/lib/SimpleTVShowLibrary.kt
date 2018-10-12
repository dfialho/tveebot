package dfialho.tveebot.library.lib

import dfialho.tveebot.library.api.TVShowLibrary
import dfialho.tveebot.library.api.TVShowUsher
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import java.nio.file.Path
import java.nio.file.Paths

class SimpleTVShowLibrary(private val libraryDirectory: Path, private val usher: TVShowUsher) : TVShowLibrary {

    override fun store(episode: TVShowEpisode, currentLocation: Path) {
        usher.store(currentLocation, libraryDirectory.resolve(getLocationOf(episode)))
    }

    override fun getLocationOf(episode: TVShowEpisode): Path {
        return with(episode) {
            Paths.get(
                tvShowTitle,
                "Season %02d".format(season),
                "$tvShowTitle - ${season}x%02d - $title".format(number)
            )
        }
    }
}