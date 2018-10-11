package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.models.TVShowID
import dfialho.tveebot.tracker.api.models.randomTVShowID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
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

    override fun get(tvShowID: TVShowID): String? = transaction(db) {
        IDs.select { IDs.id eq tvShowID }
            .map { it[IDs.providerID] }
            .firstOrNull()
    }

    override fun set(tvShowID: TVShowID, providerID: String) {
        transaction(db) {
            IDs.insert {
                it[IDs.id] = tvShowID
                it[IDs.providerID] = providerID
            }
        }
    }

    override fun getTVShowID(providerID: String): TVShowID {
        val tvShowID: TVShowID? = transaction(db) {
            IDs.select { IDs.providerID eq providerID }
                .map { it[IDs.id] }
                .firstOrNull()
        }

        return tvShowID ?: randomTVShowID().apply { set(this, providerID) }
    }
}
