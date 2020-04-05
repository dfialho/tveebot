package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.TVShow
import dfialho.tveebot.app.api.models.TVShowEntity

interface TVeebotRepository {

    fun store(tvShow: TVShowEntity): TVShow
    fun findTVShow(tvShowId: String, tracked: Boolean? = null): TVShowEntity?
    fun update(tvShow: TVShowEntity)
    fun upsert(tvShow: TVShowEntity)

    fun transaction(block: TVeebotRepository.() -> Unit)
}
