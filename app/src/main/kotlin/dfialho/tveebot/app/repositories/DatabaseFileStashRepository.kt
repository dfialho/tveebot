package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.components.FileStash
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseFileStashRepository(private val db: Database) : FileStashRepository {

    init {
        transaction {
            SchemaUtils.create(StashFiles)
        }
    }

    override fun insert(stashedFile: FileStash.StashedFile) {

        transaction {
            StashFiles.insertIgnore {
                it[NAME] = stashedFile.name
                it[FILE_ID] = stashedFile.fileId
            }
        }
    }

    override fun findByName(name: String): FileStash.StashedFile? {

        return transaction {
            StashFiles.select { StashFiles.NAME eq name }
                .limit(1)
                .mapToStashedFile()
                .firstOrNull()
        }
    }

    override fun remove(name: String) {

        transaction {
            StashFiles.deleteWhere { StashFiles.NAME eq name }
        }
    }

    override fun list(): List<FileStash.StashedFile> {

        return transaction {
            StashFiles.selectAll()
                .mapToStashedFile()
        }
    }

    private fun <R> transaction(block: DatabaseFileStashRepository.() -> R): R {

        return transaction(db) {
            block()
        }
    }

    private fun Query.mapToStashedFile(): List<FileStash.StashedFile> {

        return map { FileStash.StashedFile(it[StashFiles.NAME], it[StashFiles.FILE_ID]) }
    }
}
