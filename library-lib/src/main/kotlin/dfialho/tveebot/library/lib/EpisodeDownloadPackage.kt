package dfialho.tveebot.library.lib

import com.google.common.primitives.Longs
import dfialho.tveebot.library.api.EpisodePackage
import java.nio.file.Files
import java.nio.file.Path

/**
 * Representation of download package holding an episode file.
 *
 * Takes a path to an episode that was downloaded and returns the path to the actual episode video
 * file. The specified [path] can either be a path to a file or a directory.
 *
 * In the most simple case, if [path] is a file corresponding to the path to the actual episode
 * file. If [path] is a directory, then the episode file is expected to be stored inside that
 * directory.
 *
 * Context: This package accepts both a file or a directory because when downloading an episode
 * file, this file can be directly downloaded, which means the episode file is the actual [path],
 * or it can be downloaded inside a directory containing other files. In that case [path] would be
 * a directory.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class EpisodeDownloadPackage(override val path: Path) : EpisodePackage {

    /**
     * Returns the episode file included within the package.
     *
     * @throws IllegalStateException if an error occurs when accessing the internal path.
     */
    override fun getEpisode(): Path {

        try {
            return when {
                Files.isRegularFile(path) -> path
                Files.isDirectory(path) -> findLargestFileIn(path) ?: throw IllegalStateException("Package is an empty directory")
                Files.notExists(path) -> throw IllegalStateException("Package does not exist: $path")
                else -> throw IllegalStateException("Package is neither a directory or a regular file")
            }
        } catch (e: SecurityException) {
            throw IllegalStateException("No permission to access the package", e)
        }
    }

    private fun findLargestFileIn(directory: Path): Path? {
        return Files.list(directory)
            .max { file1, file2 -> Longs.compare(Files.size(file1), Files.size(file2)) }
            .orElse(null)
    }
}
