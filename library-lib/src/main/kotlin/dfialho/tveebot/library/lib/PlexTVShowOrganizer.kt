package dfialho.tveebot.library.lib

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.library.api.TVShowOrganizer
import java.nio.file.Path
import java.nio.file.Paths

class PlexTVShowOrganizer : TVShowOrganizer {

    override fun locationOf(episodeFile: EpisodeFile): Path {

        val baseEpisode = episodeFile.episodes[0]

        val tvShowFolder = episodeFile.tvShow.title.trim()
        val seasonFolder = "Season %02d".format(baseEpisode.season)
        val episodes = episodeFile.episodes.joinToString("-") { "e%02d".format(it.number) }
        val videoFileName = "$tvShowFolder - s%02d$episodes".format(baseEpisode.season)

        val title = baseEpisode.title.trim()
            .takeIf { episodeFile.episodes.all { it.title == baseEpisode.title } }
            .orEmpty()

        val extraInfo = if (title.isNotBlank()) {
            " - $title"
        } else {
            ""
        }

        return Paths.get(tvShowFolder, seasonFolder, videoFileName + extraInfo)
    }
}
