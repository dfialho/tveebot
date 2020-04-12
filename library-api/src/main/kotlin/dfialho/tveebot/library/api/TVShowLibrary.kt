package dfialho.tveebot.library.api

import dfialho.tveebot.app.api.models.EpisodeFile
import java.nio.file.Path

interface TVShowLibrary {
    fun store(episodeFile: EpisodeFile, videoFile: Path): Path
}
