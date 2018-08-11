package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShowIDMapper
import java.util.*

class InMemoryTVShowIDMapper : TVShowIDMapper {

    private val idMap: MutableMap<UUID, String> = mutableMapOf()
    private val reverseMap: MutableMap<String, UUID> = mutableMapOf()

    override operator fun get(uuid: UUID): String? = idMap[uuid]

    override operator fun set(uuid: UUID, providerID: String) {
        idMap[uuid] = providerID
        reverseMap[providerID] = uuid
    }

    override fun getUUID(providerID: String): UUID {
        val uuid: UUID? = reverseMap[providerID]

        return if (uuid == null) {
            val generatedUUID = UUID.randomUUID()
            set(generatedUUID, providerID)
            generatedUUID
        } else {
            uuid
        }
    }
}