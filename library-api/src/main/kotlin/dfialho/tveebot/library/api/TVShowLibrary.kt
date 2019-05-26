package dfialho.tveebot.library.api

import dfialho.tveebot.tracker.api.models.Episode

interface TVShowLibrary {
    fun store(episode: Episode, episodePackage: EpisodePackage)
}