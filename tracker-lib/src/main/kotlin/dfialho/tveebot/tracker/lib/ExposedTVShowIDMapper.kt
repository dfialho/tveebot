package dfialho.tveebot.tracker.lib

import dfialho.tveebot.application.api.ID
import dfialho.tveebot.application.api.randomID
import dfialho.tveebot.tracker.api.TVShowIDMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedTVShowIDMapper(private val db: Database) : TVShowIDMapper {

    private object IDs : Table() {
        val id = varchar("id", length = 36).primaryKey()
        val providerID = varchar("provider_id", length = 256)
    }

    init {
        transaction(db) {
            SchemaUtils.create(IDs)
        }
    }

    override fun get(tvShowID: ID): String? = transaction(db) {
        IDs.select { IDs.id eq tvShowID.value }
            .map { it[IDs.providerID] }
            .firstOrNull()
    }

    override fun set(tvShowID: ID, providerID: String) {
        transaction(db) {
            IDs.insert {
                it[IDs.id] = tvShowID.value
                it[IDs.providerID] = providerID
            }
        }
    }

    override fun getTVShowID(providerID: String): ID {
        val tvShowID: ID? = transaction(db) {
            IDs.select { IDs.providerID eq providerID }
                .map { it[IDs.id] }
                .map { ID(it) }
                .firstOrNull()
        }

        return tvShowID ?: randomID().apply { set(this, providerID) }
    }
}
