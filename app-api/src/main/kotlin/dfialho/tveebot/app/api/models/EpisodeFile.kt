package dfialho.tveebot.app.api.models

data class EpisodeFile(
    val file: VideoFile,
    val episodes: List<Episode>
) {
    init {
        require(episodes.isNotEmpty()) { "File must have at least one episode" }
        require(episodes.map { it.tvShow }.toSet().size == 1) { "All episodes must have the same TV Show" }
    }

    val tvShow: TVShow get() = episodes[0].tvShow
}
