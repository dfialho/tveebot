package dfialho.tveebot.tracker.api

import dfialho.tveebot.application.api.ID

/**
 * A [TVShowIDMapper] maps the UUID of each TV show to the ID of that TV show in a specific [TVShowProvider].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TVShowIDMapper {

    /**
     * Returns the TV show provider ID for the given [tvShowID], or null if this mapper does not hold a provider ID for
     * the given [tvShowID].
     */
    operator fun get(tvShowID: ID): String?

    /**
     * Sets the [providerID] for the given [tvShowID].
     */
    operator fun set(tvShowID: ID, providerID: String)

    /**
     * Returns the [ID] for [providerID]. If an ID has not been set before, then it generates a new one and returns
     * that. In that case, also sets the newly generated ID to [providerID].
     */
    fun getTVShowID(providerID: String): ID
}