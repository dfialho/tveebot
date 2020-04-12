package dfialho.tveebot.library.api

import dfialho.tveebot.app.api.models.EpisodeFile
import java.nio.file.Path

interface TVShowOrganizer {
    fun locationOf(episodeFile: EpisodeFile): Path
}