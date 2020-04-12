package dfialho.tveebot.app.api.models

data class EpisodeFile(
    val file: VideoFile,
    val episodes: List<Episode>
) {
    init {
        require(episodes.isNotEmpty()) { "File must have at least one episode" }
        require(episodes.allSame { it.tvShow }) { "All episodes must have the same TV Show" }
        require(episodes.allSame { it.season }) { "All episodes must be from the same season" }
    }

    val tvShow: TVShow get() = episodes[0].tvShow

    private fun <R : Any> List<Episode>.allSame(property: (Episode) -> R): Boolean {
        return this.map(property).toSet().size == 1
    }
}
