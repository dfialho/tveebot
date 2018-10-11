package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.models.TVShowID

/**
 * Implementation of [TVShowIDMapper] that does not do any special mapping. For a given ID it returns always that same
 * value.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class DirectTVShowIDMapper : TVShowIDMapper {

    override fun get(tvShowID: TVShowID): String? = tvShowID

    /**
     * This method does nothing. It ignores any provider ID specified here.
     */
    override fun set(tvShowID: TVShowID, providerID: String) {
    }

    override fun getTVShowID(providerID: String): TVShowID = providerID
}