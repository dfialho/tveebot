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
     * Inserts a batch of TV shows into the repository. Those already included in the repository will be ignored.
     */
    fun putAll(tvShows: List<TVShow>)

    /**
     * Returns the [TrackedTVShow] identified by [tvShowUUID].
     */
    fun findTrackedTVShow(tvShowUUID: UUID): TrackedTVShow?

    /**
     * Returns a list containing every TV show in the repository.
     */
    fun findAllTVShows(): List<TVShow>

    /**
     * Returns a list containing every TV show marked being tracked.
     */
    fun findTrackedTVShows(): List<TrackedTVShow>

    /**
     * Returns a list containing every TV show that is NOT marked as being tracked.
     */
    fun findNotTrackedTVShows(): List<TVShow>

    /**
     * Marks the TV show with [tvShowUUID] as being tracked and sets the video [quality] the episodes of this TV show
     * are expected to be downloaded.
     */
    fun setTracked(tvShowUUID: UUID, quality: VideoQuality)

    /**
     * Marks the TV show with [tvShowUUID] as not being tracked. If the TV show was being tracked it will lost all
     * tracking information, such as, the video quality. If the TV shows was not being tracked, then this method has
     * no effect.
     */
    fun setNotTracked(tvShowUUID: UUID)

    /**
     * Sets the [videoQuality] for the tracked TV show identified by [tvShowUUID].
     */
    fun setTVShowVideoQuality(tvShowUUID: UUID, videoQuality: VideoQuality)

    /**
     * Inserts the [episodeFile] into the repository associated with the TV show with [tvShowUUID], if it does not
     * exist yet. Otherwise it throws an exception.
     */
    fun put(tvShowUUID: UUID, episodeFile: EpisodeFile)

    /**
     * Retrieves every [EpisodeFile] from [tvShow] available in the repository. If no episode file is found then it
     * returns an empty list.
     */
    fun findEpisodeFilesFrom(tvShow: TVShow): List<EpisodeFile>

    /**
     * Removes every [EpisodeFile] from TV show identified by [tvShowUUID] available in the repository. If no episode
     * file is found then this method has no effect.
     */
    fun removeEpisodeFilesFrom(tvShowUUID: UUID)
}