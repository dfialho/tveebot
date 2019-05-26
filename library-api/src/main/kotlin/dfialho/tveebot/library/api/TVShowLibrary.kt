package dfialho.tveebot.library.api

import dfialho.tveebot.tracker.api.models.Episode
import java.nio.file.Path

interface TVShowLibrary {
    fun store(episode: Episode, episodePackage: EpisodePackage): Path
}