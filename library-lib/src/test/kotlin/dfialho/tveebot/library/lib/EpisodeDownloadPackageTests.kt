package dfialho.tveebot.library.lib

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.specs.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path

/**
 * Tests for the [EpisodeDownloadPackage].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class EpisodeDownloadPackageTests : AnnotationSpec() {

    val tmpFolder = TemporaryFolder()

    override fun beforeTest(description: Description) {
        super.beforeTest(description)
        println("before each test")
        tmpFolder.create()
    }

    override fun afterTest(description: Description, result: TestResult) {
        super.afterTest(description, result)
        println("after each test")
        tmpFolder.delete()
    }

    @Test
    fun `episode from file corresponds to the file path`() {
        tmpFolder.create()
        val filePath = tmpFolder.newFile().toPath()
        val episodePackage = EpisodeDownloadPackage(filePath)

        assert(episodePackage.getEpisode()).isEqualTo(filePath)
    }

    @Test
    fun `episode from directory containing a single video file corresponds to that video file`() {
        val downloadDirectory: Path = tmpFolder.root.toPath()
        val videoFilePath = tmpFolder.newFile().toPath()
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert(episodePackage.getEpisode()).isEqualTo(videoFilePath)
    }

    @Test
    fun `episode from directory containing a multiple video file corresponds to largest video file`() {
        val downloadDirectory: Path = tmpFolder.root.toPath()
        tmpFolder.newFile()
        val largerFile = tmpFolder.newFile()
        largerFile.writeText("Some text to make file bigger")
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert(episodePackage.getEpisode()).isEqualTo(largerFile.toPath())
    }

    @Test
    fun `episode from empty directory throws IllegalStateException`() {
        val downloadDirectory: Path = tmpFolder.root.toPath()
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert {
            episodePackage.getEpisode()
        }.thrownError {
            isInstanceOf(IllegalStateException::class)
        }
    }

    @Test
    fun `episode from non-existing path throws IllegalStateException`() {
        val downloadDirectory: Path = tmpFolder.root.toPath().resolve("fake")
        val episodePackage = EpisodeDownloadPackage(downloadDirectory)

        assert {
            episodePackage.getEpisode()
        }.thrownError {
            isInstanceOf(IllegalStateException::class)
        }
    }
}