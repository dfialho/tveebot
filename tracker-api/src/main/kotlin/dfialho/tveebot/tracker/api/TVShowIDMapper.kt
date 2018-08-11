package dfialho.tveebot.tracker.api

import java.util.*

/**
 * A [TVShowIDMapper] maps the UUID of each TV show to the ID of that TV show in a specific [TVShowProvider].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TVShowIDMapper {

    /**
     * Returns the TV show provider ID for the given [uuid], or null if this mapper does not hold a provider ID for
     * the given [uuid].
     */
    operator fun get(uuid: UUID): String?

    /**
     * Sets the [providerID] for the given [uuid].
     */
    operator fun set(uuid: UUID, providerID: String)

    /**
     * Returns the UUID for [providerID]. If an UUID has not been set before, then it generates a new one and returns
     * that. In that case, also sets the newly generated UUID to [providerID].
     */
    fun getUUID(providerID: String): UUID
}