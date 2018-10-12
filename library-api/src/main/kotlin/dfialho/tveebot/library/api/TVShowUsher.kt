package dfialho.tveebot.library.api

import java.nio.file.Path

interface TVShowUsher {
    fun store(savePath: Path, libraryLocation: Path)
}