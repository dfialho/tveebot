package dfialho.tveebot.services.models

import dfialho.tveebot.tracker.api.models.EpisodeFile

data class NewEpisodeNotification(
    val episodeFile: EpisodeFile
)
