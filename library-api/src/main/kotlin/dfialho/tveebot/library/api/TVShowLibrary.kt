package dfialho.tveebot.library.api

import dfialho.tveebot.tracker.api.models.TVShowEpisode

interface TVShowLibrary {
    fun store(episode: TVShowEpisode, episodePackage: EpisodePackage)
}