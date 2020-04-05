package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.Episode
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.TVShow

interface TVeebotRepository {

    fun store(tvShow: TVShow, tracked: Boolean = false): TVShow
    fun store(episode: Episode): Episode

    fun findEpisodes(state: State? = null): List<Episode>

    fun updateState(id: String, downloaded: State)
}
