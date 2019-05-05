package dfialho.tveebot.services

import dfialho.tveebot.application.api.EpisodeEntity
import dfialho.tveebot.application.api.TVShowEntity
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.tracker.api.models.ID
import mu.KLogging
import java.util.*

class InformationService(private val repository: TrackerRepository) : Service {

    companion object : KLogging()

    override val name: String
        get() = "Information Service"

    override fun start() = logStart(logger)
    override fun stop() = logStop(logger)

    /**
     * Returns a list with every TV show in the repository.
     */
    fun getAllTVShows(): List<TVShowEntity> = repository.findAllTVShows()

    /**
     * Returns a list containing every TV show currently being tracked.
     */
    fun getTrackedTVShows(): List<TVShowEntity> = repository.findTrackedTVShows()

    /**
     * Returns a list containing every TV show currently NOT being tracked.
     */
    fun getNotTrackedTVShows(): List<TVShowEntity> = repository.findNotTrackedTVShows()

    /**
     * Returns a map associating each TV show to its episodes. TV shows without any episode are not included in the
     * returned map.
     */
    fun getAllEpisodesByTVShow(): Map<ID, List<EpisodeEntity>> = repository.findEpisodesByTVShow().mapKeys { it.key.id }

    /**
     * Returns a list containing every episode from the TV show identified by [tvShowID].
     *
     * @throws NoSuchElementException if no TV show is found with id [tvShowID].
     */
    fun getEpisodesFrom(tvShowID: ID): List<EpisodeEntity> = repository.findEpisodesFrom(tvShowID)
}