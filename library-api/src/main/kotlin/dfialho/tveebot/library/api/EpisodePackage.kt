package dfialho.tveebot.library.api

import java.nio.file.Path

/**
 * An episode package is an abstraction for a container that holds an episode file. A simple example
 * of such a package is a directory which may contain multiple files, and one of those is an episode
 * video file.
 */
interface EpisodePackage {

    /**
     * Internal path to the package.
     */
    val path: Path

    /**
     * Returns the episode file included within the package.
     */
    fun getEpisode(): Path
}