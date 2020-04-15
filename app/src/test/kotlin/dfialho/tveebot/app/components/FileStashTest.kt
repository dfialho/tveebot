package dfialho.tveebot.app.components

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import dfialho.tveebot.app.randomInMemoryDatabase
import dfialho.tveebot.app.repositories.DatabaseFileStashRepository
import dfialho.tveebot.commons.temporaryDirectory
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files
import java.nio.file.Paths

@Suppress("BlockingMethodInNonBlockingContext")
class FileStashTest : FunSpec({

    val stashDirectory by temporaryDirectory()
    val otherDirectory by temporaryDirectory()

    test("when a file is put into the stash it is moved to the stash directory") {

        val file = Files.createFile(otherDirectory.resolve("video.mkv"))
        val stash = FileStash(stashDirectory, DatabaseFileStashRepository(randomInMemoryDatabase()))

        val stashPath = stash.put("file-id", file)

        assert(Files.exists(stashPath))
            .isTrue()

        assert(stashPath)
            .isEqualTo(Paths.get(stashDirectory.toString(), file.fileName.toString()))
    }

    test("when taking a file from the stash it use the correct path and file id") {

        val fileId = "file-id"
        val file = Files.createFile(otherDirectory.resolve("video.mkv"))
        val stash = FileStash(stashDirectory, DatabaseFileStashRepository(randomInMemoryDatabase()))
        val stashPath = stash.put(fileId, file)

        stash.takeEach { takenFileId, takenPath ->

            assert(takenPath)
                .isEqualTo(stashPath)

            assert(takenFileId)
                .isEqualTo(fileId)
        }
    }

    test("when stash directory does not exist it is created") {

        val fileId = "file-id"
        val file = Files.createFile(otherDirectory.resolve("video.mkv"))
        val stash = FileStash(stashDirectory.resolve("stash"), DatabaseFileStashRepository(randomInMemoryDatabase()))

        val stashPath = stash.put(fileId, file)

        assert(Files.exists(stashPath))
            .isTrue()
    }
})
