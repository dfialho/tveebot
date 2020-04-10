package dfialho.tveebot.tracker.api

import dfialho.tveebot.app.api.models.VideoQuality

data class MatchedEpisodeFile(
    val episodes: List<MatchedEpisode>,
    val videoQuality: VideoQuality
)

