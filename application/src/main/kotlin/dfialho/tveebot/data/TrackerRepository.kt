package dfialho.tveebot.data

import dfialho.tveebot.data.models.EpisodeDownload
import dfialho.tveebot.data.models.EpisodeEntity
import dfialho.tveebot.data.models.TVShowEntity
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.TVShowID
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tvShowEntityFrom
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
    fun put(tvShow: TVShowEntity)

    /**
     * Inserts a batch of TV shows into the repository. Those already included in the repository will be ignored.
     */
    fun putAll(tvShows: List<TVShowEntity>)

    /**
     * Returns the [tvShowEntityFrom] identified by [tvShowID].
     */
    fun findTrackedTVShow(tvShowID: TVShowID): TVShowEntity?

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
    fun setTracked(tvShowID: TVShowID, quality: VideoQuality)

    /**
     * Marks the TV show with [tvShowID] as not being tracked. If the TV show was being tracked it will lost all
     * tracking information, such as, the video quality. If the TV shows was not being tracked, then this method has
     * no effect.
     *
     * @throws IllegalStateException if the TV show with ID [tvShowID] is already not being tracked.
     * @throws NoSuchElementException if no TV show with ID [tvShowID] exists.
     */
    fun setNotTracked(tvShowID: TVShowID)

    /**
     * Sets the [videoQuality] for the tracked TV show identified by [tvShowID].
     *
     * @throws NoSuchElementException if no TV show with ID [tvShowID] is being tracked
     */
    fun setTVShowVideoQuality(tvShowID: TVShowID, videoQuality: VideoQuality)

    /**
     * Inserts the [episode] into the repository associated with the TV show with [tvShowID], if it does not
     * exist yet. Otherwise it throws an exception.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the episode. For instance, the
     * repository already contains a episode with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun put(tvShowID: TVShowID, episode: EpisodeFile)

    /**
     * Inserts the [episode] into the repository if the [predicate] returns true. It returns true if the [episode] was
     * inserted, or false if otherwise. The [predicate] is called with the old episode and the new before inserting.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the episode. For instance, the
     * repository already contains a episode with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun putOrUpdateIf(tvShowID: TVShowID, episode: EpisodeFile, predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean): Boolean

    /**
     * Retrieves every episode from TV show identified by [tvShowID] available in the repository. If no episode file
     * is found then it returns an empty list.
     *
     * @throws TrackerRepositoryException if some error occurs when executing this operation.
     * @throws NoSuchElementException if the repository does not contain a TV show with the specified [tvShowID].
     */
    fun findEpisodesFrom(tvShowID: TVShowID): List<EpisodeEntity>

//    TODO
//    fun findEpisodesFrom(tvShowID: TVShowID, quality: VideoQuality): List<EpisodeEntity>

    /**
     * Retrieves every episode in the repository and groups it by TV show.
     */
    fun findEpisodesByTVShow(): Map<TVShowEntity, List<EpisodeEntity>>

    /**
     * Removes every episode from TV show identified by [tvShowID] available in the repository. If no episode
     * file is found then this method has no effect. If the episode has downloads associated with it, then those are
     * removed too.
     */
    fun removeEpisodesFrom(tvShowID: TVShowID)

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
    fun findDownloadsFrom(tvShowID: TVShowID): List<EpisodeDownload>

    /**
     * Removes the download with the given [reference]. If the repository does not contain a download with [reference],
     * then this method has no effect.
     */
    fun removeDownload(reference: DownloadReference)

    /**
     * Removes every download of an episode from the TV show identified by [tvShowID].
     */
    fun removeAllDownloadsFrom(tvShowID: TVShowID)

    /**
     * Remove everything from the repository.
     */
    fun clearAll()
}