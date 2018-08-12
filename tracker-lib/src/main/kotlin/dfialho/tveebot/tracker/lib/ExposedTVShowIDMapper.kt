package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShowIDMapper
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import java.util.UUID.randomUUID

@Component
@Transactional
class ExposedTVShowIDMapper(private val transactionTemplate: TransactionTemplate)
    : TVShowIDMapper, InitializingBean {

    private object IDs : Table() {
        val uuid = uuid("uuid").primaryKey()
        val providerID = varchar("provider_id", length = 256)
    }

    override fun afterPropertiesSet() {
        transactionTemplate.execute {
            SchemaUtils.create(IDs)
        }
    }

    override fun get(uuid: UUID): String? = IDs.select { IDs.uuid eq uuid }
        .map { it[IDs.providerID] }
        .firstOrNull()

    override fun set(uuid: UUID, providerID: String) {
        IDs.insert {
            it[IDs.uuid] = uuid
            it[IDs.providerID] = providerID
        }
    }

    override fun getUUID(providerID: String): UUID {
        val uuid: UUID? = IDs.select { IDs.providerID eq providerID }
            .map { it[IDs.uuid] }
            .firstOrNull()

        return uuid ?: randomUUID().apply { set(this, providerID) }
    }
}
