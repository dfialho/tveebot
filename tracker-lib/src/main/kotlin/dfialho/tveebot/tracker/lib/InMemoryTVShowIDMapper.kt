package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShowIDMapper
import java.util.*

class InMemoryTVShowIDMapper : TVShowIDMapper {

    private val idMap: MutableMap<UUID, String> = hashMapOf()

    override operator fun get(uuid: UUID): String? {
        return idMap[uuid]
    }

    override operator fun set(uuid: UUID, providerID: String) {
        idMap[uuid] = providerID
    }
}