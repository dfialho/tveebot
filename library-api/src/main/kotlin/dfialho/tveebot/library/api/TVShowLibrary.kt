package dfialho.tveebot.library.api

import dfialho.tveebot.tracker.api.models.TVShowEpisode
import java.nio.file.Path

interface TVShowLibrary {
    fun store(episode: TVShowEpisode, currentLocation: Path)
    fun getLocationOf(episode: TVShowEpisode): Path
}