package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.*

interface TVeebotRepository {

    fun findTVShow(tvShowId: String, tracked: Boolean? = null): TVShowEntity?
    fun findTVShows(tracked: Boolean? = null): List<TVShowEntity>

    fun upsert(tvShow: TVShowEntity)
    fun update(episode: EpisodeEntity)

    fun insert(episodeFile: EpisodeFile)
    fun findEpisodeFiles(tvShowId: String, state: State, videoQuality: VideoQuality): List<EpisodeFile>
    fun findEpisode(id: String): EpisodeEntity?
    fun findEpisodeLatestFile(id: String): VideoFile?
    fun findEpisodesByFile(fileId: String): List<EpisodeEntity>

    fun <T> transaction(block: TVeebotRepository.() -> T): T
}
