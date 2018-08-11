package dfialho.tveebot.tracker.api

import java.util.*

/**
 * A tracker repository holds state information required by a [TrackerEngine] to track a set of TV show episodes. It
 * exposes a set of methods to add information to the repository, such as, [TVShow]s and [EpisodeFile]s. As well as
 * methods to retrieve this same information from it.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TrackerRepository {

    /**
     * Inserts the [tvShow] into the repository, if it does not exist yet. Otherwise, it throws an exception.
     * According to [tracked], the TV show is marked as being "tracked" or not. By default, the TV show is marked
     * as "not tracked".
     *
     * @throws
     */
    fun put(tvShow: TVShow, tracked: Boolean = false)

    /**
     * Inserts a batch of TV shows into the repository. Those already included in the repository will be ignored.
     */
    fun putAll(tvShows: List<TVShow>)

    /**
     * Retrieves every TV show in the repository.
     */
    fun findAllTVShows(): List<TVShow>

    /**
     * Retrieves every TV show marked as either "tracked" (if [tracked] is true) or "not tracked" (if [tracked] is
     * false).
     */
    fun findTVShows(tracked: Boolean): List<TVShow>

    /**
     * Sets the TV show with [tvShowUUID] as either "tracker" or "not tracked" according to [tracked].
     */
    fun setTracked(tvShowUUID: UUID, tracked: Boolean = true)

    /**
     * Inserts the [episodeFile] into the repository associated with [tvShow], if it does not exist yet. Otherwise it
     * throws an exception.
     *
     * @throws
     */
    fun put(tvShow: TVShow, episodeFile: EpisodeFile)

    /**
     * Retrieves every [EpisodeFile] from [tvShow] available in the repository. If no episode file is found then it
     * returns an empty list.
     */
    fun findEpisodeFilesFrom(tvShow: TVShow): List<EpisodeFile>
}