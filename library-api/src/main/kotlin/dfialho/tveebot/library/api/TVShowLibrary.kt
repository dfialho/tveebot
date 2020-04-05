package dfialho.tveebot.library.api

import dfialho.tveebot.app.api.models.Episode
import java.nio.file.Path

interface TVShowLibrary {
    fun store(episodes: List<Episode>, episodePackage: EpisodePackage): Path
}