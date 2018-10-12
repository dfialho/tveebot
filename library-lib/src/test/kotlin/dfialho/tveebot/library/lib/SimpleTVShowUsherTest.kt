package dfialho.tveebot.library.lib

import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests for the [SimpleTVShowUsher].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class SimpleTVShowUsherTest {

    private val usher = SimpleTVShowUsher()

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun findEpisodeFileFromPathToFile() {
        val episodePath: Path = tmpFolder.newFile().toPath()

        val actualPath = usher.findEpisodeFile(episodePath)

        actualPath.shouldEqual(episodePath)
    }

    @Test
    fun findEpisodeFileFromDirectoryWithSingleFile() {
        val episodePath: Path = tmpFolder.root.toPath()
        val expectedPath = tmpFolder.newFile().toPath()

        val actualPath = usher.findEpisodeFile(episodePath)

        actualPath.shouldEqual(expectedPath)
    }

    @Test
    fun findEpisodeFileFromDirectoryWithTwoFiles() {
        val episodePath: Path = tmpFolder.root.toPath()
        tmpFolder.newFile()
        val largerFile = tmpFolder.newFile()
        largerFile.writeText("Some text to make file bigger")

        val actualPath = usher.findEpisodeFile(episodePath)

        actualPath.shouldEqual(largerFile.toPath())
    }

    @Test
    fun findEpisodeFileFromEmptyDirectory() {
        val episodePath: Path = tmpFolder.root.toPath()

        val operation = { usher.findEpisodeFile(episodePath) }

        operation.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun findEpisodeFileFromNonExistingPath() {
        val episodePath: Path = tmpFolder.root.toPath().resolve("fake")

        val operation = { usher.findEpisodeFile(episodePath) }

        operation.shouldThrow(FileNotFoundException::class)
    }

    @Test
    fun storeEpisodeFile() {
        val extension = "mkv"
        val episodePath = tmpFolder.newFile("original-file.$extension").toPath()

        val libraryLocationDirectory = tmpFolder.newFolder().toPath()
        val newFileName = "episode 1x01"
        val libraryLocation = libraryLocationDirectory.resolve(newFileName)

        usher.store(
            savePath = episodePath,
            libraryLocation = libraryLocation
        )

        Files.exists(libraryLocationDirectory.resolve("$newFileName.$extension")).shouldBeTrue()
        Files.exists(episodePath).shouldBeFalse()
    }

    @Test
    fun storeEpisodeDirectory() {
        val episodeDirectory = tmpFolder.newFolder().toPath()
        val extension = "mkv"
        Files.createFile(episodeDirectory.resolve("video.$extension"))

        val libraryLocationDirectory = tmpFolder.newFolder().toPath()
        val newFileName = "episode 1x01"
        val libraryLocation = libraryLocationDirectory.resolve(newFileName)

        usher.store(
            savePath = episodeDirectory,
            libraryLocation = libraryLocation
        )

        Files.exists(libraryLocationDirectory.resolve("$newFileName.$extension")).shouldBeTrue()
        Files.exists(episodeDirectory).shouldBeFalse()
    }

    @Test
    fun storeEpisodeDirectoryWithMultipleFiles() {
        val episodeDirectory = tmpFolder.newFolder().toPath()
        val extension = "mkv"
        val videoFile = episodeDirectory.resolve("video.$extension")
        Files.createFile(videoFile)
        videoFile.toFile().writeText("Some text to make file bigger")
        Files.createFile(episodeDirectory.resolve("other-file.txt"))

        val libraryLocationDirectory = tmpFolder.newFolder().toPath()
        val newFileName = "episode 1x01"
        val libraryLocation = libraryLocationDirectory.resolve(newFileName)

        usher.store(
            savePath = episodeDirectory,
            libraryLocation = libraryLocation
        )

        Files.exists(libraryLocationDirectory.resolve("$newFileName.$extension")).shouldBeTrue()
        Files.exists(episodeDirectory).shouldBeFalse()
    }
}