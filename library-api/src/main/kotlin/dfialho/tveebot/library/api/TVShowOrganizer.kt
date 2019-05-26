package dfialho.tveebot.library.api

import dfialho.tveebot.tracker.api.models.Episode
import java.nio.file.Path

interface TVShowOrganizer {
    fun getLocationOf(episode: Episode): Path
}