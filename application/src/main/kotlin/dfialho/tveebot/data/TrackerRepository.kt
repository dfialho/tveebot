package dfialho.tveebot.data

import dfialho.tveebot.application.api.EpisodeDownload
import dfialho.tveebot.application.api.EpisodeEntity
import dfialho.tveebot.application.api.TVShowEntity
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tvShowEntityOf
import dfialho.tveebot.utils.Result
import java.util.*

/**
 * A tracker repository holds state information required by a [TrackerEngine] to track a set of TV show episodes. It
 * exposes a set of methods to add information to the repository, such as, [TVShow]s and [EpisodeEntity]s. As well as
 * methods to retrieve this same information from it.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface TrackerRepository {

    /**
     * Inserts a new TV show into the repository.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the TV show. For instance, the
     * repository already contains a TV show with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun put(tvShow: TVShowEntity): Result

    /**
     * Inserts a batch of TV shows into the repository. Those already included in the repository will be ignored.
     */
    fun putAll(tvShows: List<TVShowEntity>)

    /**
     * Returns the [tvShowEntityOf] identified by [tvShowID].
     */
    fun findTrackedTVShow(tvShowID: ID): TVShowEntity?

    /**
     * Returns a list containing every TV show in the repository.
     */
    fun findAllTVShows(): List<TVShowEntity>

    /**
     * Returns a list containing every TV show marked being tracked.
     */
    fun findTrackedTVShows(): List<TVShowEntity>

    /**
     * Returns a list containing every TV show that is NOT marked as being tracked.
     */
    fun findNotTrackedTVShows(): List<TVShowEntity>

    /**
     * Marks the TV show with [tvShowID] as being tracked and sets the video [quality] the episodes of this TV show
     * are expected to be downloaded.
     *
     * @throws IllegalStateException if the TV show with ID [tvShowID] is already being tracked.
     * @throws NoSuchElementException if no TV show with ID [tvShowID] exists.
     */
    fun setTracked(tvShowID: ID, quality: VideoQuality)

    /**
     * Marks the TV show with [tvShowID] as not being tracked. If the TV show was being tracked it will lost all
     * tracking information, such as, the video quality. If the TV shows was not being tracked, then this method has
     * no effect.
     *
     * @throws IllegalStateException if the TV show with ID [tvShowID] is already not being tracked.
     * @throws NoSuchElementException if no TV show with ID [tvShowID] exists.
     */
    fun setNotTracked(tvShowID: ID)

    /**
     * Sets the [videoQuality] for the tracked TV show identified by [tvShowID].
     *
     * @throws NoSuchElementException if no TV show with ID [tvShowID] is being tracked
     */
    fun setTVShowVideoQuality(tvShowID: ID, videoQuality: VideoQuality)

    /**
     * Inserts the [episode] into the repository associated with the TV show with [tvShowID], if it does not
     * exist yet. Otherwise it throws an exception.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the episode. For instance, the
     * repository already contains a episode with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun put(tvShowID: ID, episode: EpisodeFile): Result

    /**
     * Inserts the [episode] into the repository, if it does not exist yet. Otherwise it throws an exception.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the episode. For instance, the
     * repository already contains a episode with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun put(episode: TVShowEpisodeFile): Result

    /**
     * Updates the episode corresponding to the specified [episode] if the specified [predicate]
     * evaluates to true.
     */
    fun updateIf(episode: TVShowEpisodeFile, predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean): Result

    /**
     * Inserts the [episode] into the repository if the [predicate] returns true. It returns true if the [episode] was
     * inserted, or false if otherwise. The [predicate] is called with the old episode and the new before inserting.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the episode. For instance, the
     * repository already contains a episode with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun putOrUpdateIf(tvShowID: ID, episode: EpisodeFile, predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean): Boolean

    /**
     * Retrieves every episode from TV show identified by [tvShowID] available in the repository. If no episode file
     * is found then it returns an empty list.
     *
     * @throws TrackerRepositoryException if some error occurs when executing this operation.
     * @throws NoSuchElementException if the repository does not contain a TV show with the specified [tvShowID].
     */
    fun findEpisodesFrom(tvShowID: ID): List<EpisodeEntity>

//    TODO
//    fun findEpisodesFrom(tvShowID: ID, quality: VideoQuality): List<EpisodeEntity>

    /**
     * Retrieves every episode in the repository and groups it by TV show.
     */
    fun findEpisodesByTVShow(): Map<TVShowEntity, List<EpisodeEntity>>

    /**
     * Removes every episode from TV show identified by [tvShowID] available in the repository. If no episode
     * file is found then this method has no effect. If the episode has downloads associated with it, then those are
     * removed too.
     */
    fun removeEpisodesFrom(tvShowID: ID)

    /**
     * Inserts the episode [download] into the repository.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the download. For instance, the
     * repository already contains a download for this episode.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the download
     * reference is too long.
     */
    fun put(download: EpisodeDownload)

    /**
     * Returns the episode download corresponding to the specified [reference], or null if no download is found.
     */
    fun findDownload(reference: DownloadReference): EpisodeDownload?

    /**
     * Returns a list containing every episode download in the repository.
     */
    fun findAllDownloads(): List<EpisodeDownload>

    /**
     * Returns a list containing every download in the repository corresponding to an episode from the TV show
     * identified by [tvShowID].
     */
    fun findDownloadsFrom(tvShowID: ID): List<EpisodeDownload>

    /**
     * Removes the download with the given [reference]. If the repository does not contain a download with [reference],
     * then this method has no effect.
     */
    fun removeDownload(reference: DownloadReference)

    /**
     * Removes the downloads with the given [references]. If the repository does not contain a download with one
     * of the specified [references], then that reference is ignored.
     */
    fun removeAllDownloads(references: List<DownloadReference>)

    /**
     * Removes every download of an episode from the TV show identified by [tvShowID].
     */
    fun removeAllDownloadsFrom(tvShowID: ID)

    /**
     * Remove everything from the repository.
     */
    fun clearAll()
}