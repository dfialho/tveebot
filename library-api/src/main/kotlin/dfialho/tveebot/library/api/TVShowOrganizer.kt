package dfialho.tveebot.library.api

import dfialho.tveebot.app.api.models.Episode
import java.nio.file.Path

interface TVShowOrganizer {
    fun getLocationOf(episode: Episode): Path
}