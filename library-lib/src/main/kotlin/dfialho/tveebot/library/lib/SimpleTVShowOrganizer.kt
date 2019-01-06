package dfialho.tveebot.library.lib

import com.google.common.annotations.VisibleForTesting
import com.google.common.primitives.Longs
import dfialho.tveebot.library.api.TVShowOrganizer
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import com.google.common.io.Files as GFiles

class SimpleTVShowOrganizer : TVShowOrganizer {

    override fun getLocationOf(episode: TVShowEpisode): Path {
        return with(episode) {
            Paths.get(
                tvShowTitle,
                "Season %02d".format(season),
                "$tvShowTitle - ${season}x%02d - $title".format(number)
            )
        }
    }

    private fun store(savePath: Path, libraryLocation: Path) {
        val episodePath = findEpisodeFile(savePath)
        val extension = GFiles.getFileExtension(episodePath.fileName.toString())
        val outputPath = Paths.get(libraryLocation.toString() + ".$extension")

        Files.createDirectories(outputPath.parent)
        Files.move(episodePath, outputPath, StandardCopyOption.REPLACE_EXISTING)
        savePath.toFile().deleteRecursively()
    }

    /**
     * Takes a path to an episode that was downloaded and returns the path to the actual episode
     * video file. The specified [savePath] can either be a path to a file or a directory.
     *
     * In the most simple case, if [savePath] is a file, and the function returns [savePath]. If
     * [savePath] is a directory, then it looks for the episode file inside that directory and
     * returns the path to that specific file.
     *
     * Context: This helper function is necessary because when downloading an episode file, this
     * file can be directly downloaded, which means the episode file is the actual [savePath], or it
     * can be downloaded inside a directory containing other files. In that case [savePath] would be
     * a directory.
     *
     * @throws FileNotFoundException if the provided path does not exist.
     * @throws IllegalArgumentException if it cannot determine the episode file based on [savePath].
     * @throws SecurityException if it cannot access the [savePath].
     */
    @VisibleForTesting
    internal fun findEpisodeFile(savePath: Path): Path = when {
        Files.isRegularFile(savePath) -> savePath
        Files.isDirectory(savePath) -> findLargestFileIn(savePath) ?: throw IllegalArgumentException("Save path is an empty directory")
        Files.notExists(savePath) -> throw FileNotFoundException("Save path does not exist: $savePath")
        else -> throw IllegalArgumentException("Save path is neither a directory or a regular file")
    }

    private fun findLargestFileIn(directory: Path): Path? {
        return Files.list(directory)
            .max { file1, file2 -> Longs.compare(Files.size(file1), Files.size(file2)) }
            .orElse(null)
    }
}
