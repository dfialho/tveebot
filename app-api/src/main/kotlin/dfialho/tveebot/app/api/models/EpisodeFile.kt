package dfialho.tveebot.app.api.models

data class EpisodeFile(
    val file: VideoFile,
    val episodes: List<Episode>
)
