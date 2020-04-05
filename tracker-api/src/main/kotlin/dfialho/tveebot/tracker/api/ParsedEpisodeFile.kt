package dfialho.tveebot.tracker.api

import dfialho.tveebot.app.api.models.VideoQuality

data class ParsedEpisodeFile(
    val episodes: List<ParsedEpisode>,
    val quality: VideoQuality
)

