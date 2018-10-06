package dfialho.tveebot.services.models

import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.VideoQuality

data class NewEpisodeParameters(
    val episode: TVShowEpisodeFile,
    val tvShowVideoQuality: VideoQuality
)
