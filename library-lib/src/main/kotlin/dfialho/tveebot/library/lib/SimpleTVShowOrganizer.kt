package dfialho.tveebot.library.lib

import dfialho.tveebot.app.api.models.Episode
import dfialho.tveebot.library.api.TVShowOrganizer
import java.nio.file.Path
import java.nio.file.Paths

class SimpleTVShowOrganizer : TVShowOrganizer {

    override fun getLocationOf(episode: Episode): Path = with(episode) {
        Paths.get(tvShow.title, "Season %02d".format(season), "${tvShow.title} - ${season}x%02d - $title".format(number))
    }
}
