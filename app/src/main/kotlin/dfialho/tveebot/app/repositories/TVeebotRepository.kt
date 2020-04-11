package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.EpisodeEntity
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShowEntity
import dfialho.tveebot.app.api.models.VideoFile

interface TVeebotRepository {

    fun findTVShow(tvShowId: String, tracked: Boolean? = null): TVShowEntity?
    fun upsert(tvShow: TVShowEntity)

    fun update(episode: EpisodeEntity)
    fun insert(episodeFile: EpisodeFile)

    fun findEpisodeLatestFile(id: String): VideoFile?
    fun findEpisode(id: String): EpisodeEntity?
    fun <T> transaction(block: TVeebotRepository.() -> T): T
}
