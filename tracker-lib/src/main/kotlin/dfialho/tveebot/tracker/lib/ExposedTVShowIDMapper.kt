package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShowIDMapper
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.UUID.randomUUID

class ExposedTVShowIDMapper(private val db: Database) : TVShowIDMapper {

    private object IDs : Table() {
        val uuid = uuid("uuid").primaryKey()
        val providerID = varchar("provider_id", length = 256)
    }

    init {
        transaction(db) {
            SchemaUtils.create(IDs)
        }
    }

    override fun get(uuid: UUID): String? = transaction(db) {
        IDs.select { IDs.uuid eq uuid }
            .map { it[IDs.providerID] }
            .firstOrNull()
    }

    override fun set(uuid: UUID, providerID: String) {
        transaction(db) {
            IDs.insert {
                it[IDs.uuid] = uuid
                it[IDs.providerID] = providerID
            }
        }
    }

    override fun getUUID(providerID: String): UUID {
        val uuid: UUID? = transaction(db) {
            IDs.select { IDs.providerID eq providerID }
                .map { it[IDs.uuid] }
                .firstOrNull()
        }

        return uuid ?: randomUUID().apply { set(this, providerID) }
    }
}
