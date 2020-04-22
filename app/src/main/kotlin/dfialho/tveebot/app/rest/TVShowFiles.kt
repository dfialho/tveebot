package dfialho.tveebot.app.rest

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShow

data class TVShowFiles(
    val tvShow: TVShow,
    val episodeFiles : List<EpisodeFile>
)
