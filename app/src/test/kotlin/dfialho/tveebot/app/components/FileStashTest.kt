package dfialho.tveebot.app.components

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.api.models.anyEpisodeFile
import dfialho.tveebot.app.randomInMemoryDatabase
import dfialho.tveebot.app.repositories.DatabaseFileStashRepository
import dfialho.tveebot.app.repositories.DatabaseTVeebotRepository
import dfialho.tveebot.commons.temporaryDirectory
import io.kotest.core.spec.style.FunSpec
import org.junit.jupiter.api.fail
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

@Suppress("BlockingMethodInNonBlockingContext")
class FileStashTest : FunSpec({

    val stashDirectory by temporaryDirectory()
    val otherDirectory by temporaryDirectory()

    test("when a file is put into the stash it is moved to the stash directory") {

        val file = Files.createFile(otherDirectory.resolve("video.mkv"))
        val episodeFile = anyEpisodeFile()
        val db = randomInMemoryDatabase()
        val repository = DatabaseTVeebotRepository(db).apply {
            upsert(TVShowEntity(episodeFile.tvShow))
            insert(episodeFile)
        }
        val stash = FileStash(stashDirectory, DatabaseFileStashRepository(db, repository))

        val reference = stash.put(file, episodeFile)

        stash.take(reference) { takenPath, takenEpisodeFile ->

            val stashPath = Paths.get(stashDirectory.toString(), file.fileName.toString())

            assert(takenPath)
                .isEqualTo(stashPath)

            assert(Files.exists(stashPath))
                .isTrue()

            assert(takenEpisodeFile)
                .isEqualTo(episodeFile)
        }
    }

    test("when a file is taken from the stash it is not listed again") {

        val file = Files.createFile(otherDirectory.resolve("video.mkv"))
        val episodeFile = anyEpisodeFile()
        val db = randomInMemoryDatabase()
        val repository = DatabaseTVeebotRepository(db).apply {
            upsert(TVShowEntity(episodeFile.tvShow))
            insert(episodeFile)
        }
        val stash = FileStash(stashDirectory, DatabaseFileStashRepository(db, repository))

        val reference = stash.put(file, episodeFile)

        stash.take(reference) { _, _ -> }

        assert {
            stash.take(reference) { _, _ -> }
        }.thrownError {
            isInstanceOf(FileNotFoundException::class)
        }

        stash.takeEach { _, _ -> fail { "No file should be in the stash" } }
    }
})
