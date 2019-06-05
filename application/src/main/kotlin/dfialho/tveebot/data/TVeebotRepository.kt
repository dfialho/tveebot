package dfialho.tveebot.data

import dfialho.tveebot.application.api.TVShowEntity

interface TVeebotRepository {

    fun listAllTVShows(): List<TVShowEntity>

    fun listTrackedTVShows(): List<TVShowEntity>

    fun listNonTrackedTVShows(): List<TVShowEntity>
}