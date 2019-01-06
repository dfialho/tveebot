package dfialho.tveebot.library.api

import dfialho.tveebot.tracker.api.models.TVShowEpisode
import java.nio.file.Path

interface TVShowOrganizer {
    fun getLocationOf(episode: TVShowEpisode): Path
}