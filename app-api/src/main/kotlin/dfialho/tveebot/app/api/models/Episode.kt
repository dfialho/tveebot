package dfialho.tveebot.app.api.models

data class Episode(
    val tvShow: TVShow,
    val season: Int,
    val number: Int,
    val title: String
) {
    val id: String = "${tvShow.id}-${season}-${number}"
}
