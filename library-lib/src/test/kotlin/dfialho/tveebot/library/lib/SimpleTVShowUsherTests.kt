package dfialho.tveebot.library.lib

import assertk.Assert
import assertk.assert
import assertk.assertAll
import assertk.assertions.exists
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.support.expected
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests for the [SimpleTVShowUsher].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class SimpleTVShowUsherTests {

    private val usher = SimpleTVShowUsher()

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun `finding episode file in a file should return path to that file`() {
        val episodePath: Path = tmpFolder.newFile().toPath()

        val actualPath = usher.findEpisodeFile(episodePath)

        assert(actualPath).isEqualTo(episodePath)
    }

    @Test
    fun `finding episode file in a directory with a single file should return path to that single file`() {
        val episodePath: Path = tmpFolder.root.toPath()
        val expectedPath = tmpFolder.newFile().toPath()

        val actualPath = usher.findEpisodeFile(episodePath)

        assert(actualPath).isEqualTo(expectedPath)
    }

    @Test
    fun `finding episode file in a directory with two files should return the biggest file of th two`() {
        val episodePath: Path = tmpFolder.root.toPath()
        tmpFolder.newFile()
        val largerFile = tmpFolder.newFile()
        largerFile.writeText("Some text to make file bigger")

        val actualPath = usher.findEpisodeFile(episodePath)

        assert(actualPath).isEqualTo(largerFile.toPath())
    }

    @Test
    fun `finding episode file from an empty directory should throw an IllegalArgumentException`() {
        val episodePath: Path = tmpFolder.root.toPath()

        assert {
            usher.findEpisodeFile(episodePath)
        }.thrownError {
            isInstanceOf(IllegalArgumentException::class)
        }
    }

    @Test
    fun `finding episode file from a non-existing path should throw FileNotFoundException`() {
        val episodePath: Path = tmpFolder.root.toPath().resolve("fake")

        assert {
            usher.findEpisodeFile(episodePath)
        }.thrownError {
            isInstanceOf(FileNotFoundException::class)
        }
    }

    @Test
    fun `storing an episode given a file path should delete initial path`() {
        val extension = "mkv"
        val episodePath = tmpFolder.newFile("original-file.$extension").toPath()

        val libraryLocationDirectory = tmpFolder.newFolder().toPath()
        val newFileName = "episode 1x01"
        val libraryLocation = libraryLocationDirectory.resolve(newFileName)
        val destinationPath = libraryLocationDirectory.resolve("$newFileName.$extension")

        usher.store(
            savePath = episodePath,
            libraryLocation = libraryLocation
        )

        assertAll {
            assert(destinationPath.toFile()).exists()
            assert(episodePath.toFile()).notExists()
        }
    }

    @Test
    fun `storing an episode given a directory path should delete initial directory`() {
        val episodeDirectory = tmpFolder.newFolder().toPath()
        val extension = "mkv"
        Files.createFile(episodeDirectory.resolve("video.$extension"))

        val libraryLocationDirectory = tmpFolder.newFolder().toPath()
        val newFileName = "episode 1x01"
        val libraryLocation = libraryLocationDirectory.resolve(newFileName)
        val destinationPath = libraryLocationDirectory.resolve("$newFileName.$extension")

        usher.store(
            savePath = episodeDirectory,
            libraryLocation = libraryLocation
        )

        assertAll {
            assert(destinationPath.toFile()).exists()
            assert(episodeDirectory.toFile()).notExists()
        }
    }

    @Test
    fun `storing an episode given a directory with multiple files`() {
        val episodeDirectory = tmpFolder.newFolder().toPath()
        val extension = "mkv"
        val videoFile = episodeDirectory.resolve("video.$extension")
        Files.createFile(videoFile)
        videoFile.toFile().writeText("Some text to make file bigger")
        Files.createFile(episodeDirectory.resolve("other-file.txt"))

        val libraryLocationDirectory = tmpFolder.newFolder().toPath()
        val newFileName = "episode 1x01"
        val libraryLocation = libraryLocationDirectory.resolve(newFileName)
        val destinationPath = libraryLocationDirectory.resolve("$newFileName.$extension")

        usher.store(
            savePath = episodeDirectory,
            libraryLocation = libraryLocation
        )

        assertAll {
            assert(destinationPath.toFile()).exists()
            assert(episodeDirectory.toFile()).notExists()
        }
    }

    private fun Assert<File>.notExists() {
        if (!actual.exists()) {
            return
        }
        expected("to not exist")
    }
}