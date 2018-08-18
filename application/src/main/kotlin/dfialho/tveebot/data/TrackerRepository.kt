package dfialho.tveebot.data

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.services.downloader.EpisodeDownload
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackedTVShow
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.VideoQuality
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
     * Inserts a new TV show into the repository. The TV show will not be tracked after being inserted.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the TV show. For instance, the
     * repository already contains a TV show with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun put(tvShow: TVShow)

    /**
     * Inserts a new TV show into the repository to be tracked. The TV show will be tracked after being inserted.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the TV show. For instance, the
     * repository already contains a TV show with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun put(tvShow: TrackedTVShow)

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
     *
     * @throws TrackerRepositoryException if no TV show with ID [tvShowUUID] is being tracked
     */
    fun setTVShowVideoQuality(tvShowUUID: UUID, videoQuality: VideoQuality)

    /**
     * Inserts the [episode] into the repository associated with the TV show with [tvShowUUID], if it does not
     * exist yet. Otherwise it throws an exception.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the episode. For instance, the
     * repository already contains a episode with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun put(tvShowUUID: UUID, episode: EpisodeFile)

    /**
     * Inserts the [episode] into the repository if the [predicate] returns true. It returns true if the [episode] was
     * inserted, or false if otherwise. The [predicate] is called with the old episode and the new before inserting.
     *
     * @throws TrackerRepositoryException if some error occurs when trying to insert the episode. For instance, the
     * repository already contains a episode with the same ID.
     * @throws IllegalArgumentException if the format of some of the parameters is invalid. For instance, the title
     * is too long.
     */
    fun putOrUpdateIf(tvShowUUID: UUID, episode: EpisodeFile, predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean): Boolean

    /**
     * Retrieves every episode from TV show identified by [tvShowUUID] available in the repository. If no episode file
     * is found then it returns an empty list.
     */
    fun findEpisodesFrom(tvShowUUID: UUID): List<EpisodeFile>

    /**
     * Removes every episode from TV show identified by [tvShowUUID] available in the repository. If no episode
     * file is found then this method has no effect. If the episode has downloads associated with it, then those are
     * removed too.
     */
    fun removeEpisodesFrom(tvShowUUID: UUID)

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
     * identified by [tvShowUUID].
     */
    fun findDownloadsFrom(tvShowUUID: UUID): List<EpisodeDownload>

    /**
     * Removes the download with the given [reference]. If the repository does not contain a download with [reference],
     * then this method has no effect.
     */
    fun removeDownload(reference: DownloadReference)

    /**
     * Removes every download of an episode from the TV show identified by [tvShowUUID].
     */
    fun removeAllDownloadsFrom(tvShowUUID: UUID)

    /**
     * Remove everything from the repository.
     */
    fun clearAll()
}