package dfialho.tveebot.tracker.api

import java.util.*

interface TVShowIDMapper {

    operator fun get(uuid: UUID): String?

    operator fun set(uuid: UUID, providerID: String)

    fun getUUID(providerID: String): UUID
}