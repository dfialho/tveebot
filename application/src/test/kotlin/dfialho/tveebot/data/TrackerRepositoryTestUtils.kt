package dfialho.tveebot.data

import dfialho.tveebot.TVeebotApplication
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.services.downloader.EpisodeDownload
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.VideoQuality
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * A wrapper around a [TrackerRepository] to ensure every operation is executed in a transactional context.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
private class Transactional(private val repository: TrackerRepository) : TrackerRepository {
    override fun put(tvShow: TVShow) = transaction { repository.put(tvShow) }
    override fun putAll(tvShows: List<TVShow>) = transaction { repository.putAll(tvShows) }
    override fun findTrackedTVShow(tvShowUUID: UUID): TVShow? = transaction { repository.findTrackedTVShow(tvShowUUID) }
    override fun findAllTVShows() = transaction { repository.findAllTVShows() }
    override fun findTrackedTVShows(): List<TVShow> = transaction { repository.findTrackedTVShows() }
    override fun findNotTrackedTVShows() = transaction { repository.findNotTrackedTVShows() }
    override fun setTracked(tvShowUUID: UUID, quality: VideoQuality) = transaction { repository.setTracked(tvShowUUID, quality) }
    override fun setNotTracked(tvShowUUID: UUID) = transaction { repository.setNotTracked(tvShowUUID) }
    override fun setTVShowVideoQuality(tvShowUUID: UUID, videoQuality: VideoQuality) = transaction { repository.setTVShowVideoQuality(tvShowUUID, videoQuality) }
    override fun put(tvShowUUID: UUID, episode: EpisodeFile) = transaction { repository.put(tvShowUUID, episode) }
    override fun putOrUpdateIf(tvShowUUID: UUID, episode: EpisodeFile, predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean) = transaction { repository.putOrUpdateIf(tvShowUUID, episode, predicate) }
    override fun findEpisodesFrom(tvShowUUID: UUID) = transaction { repository.findEpisodesFrom(tvShowUUID) }
    override fun findEpisodesByTVShow() = transaction { repository.findEpisodesByTVShow() }
    override fun removeEpisodesFrom(tvShowUUID: UUID) = transaction { repository.removeEpisodesFrom(tvShowUUID) }
    override fun put(download: EpisodeDownload) = transaction { repository.put(download) }
    override fun findDownload(reference: DownloadReference) = transaction { repository.findDownload(reference) }
    override fun findAllDownloads() = transaction { repository.findAllDownloads() }
    override fun findDownloadsFrom(tvShowUUID: UUID) = transaction { repository.findDownloadsFrom(tvShowUUID) }
    override fun removeDownload(reference: DownloadReference) = transaction { repository.removeDownload(reference) }
    override fun removeAllDownloadsFrom(tvShowUUID: UUID) = transaction { repository.removeAllDownloadsFrom(tvShowUUID) }
    override fun clearAll() = transaction { repository.clearAll() }

}

/**
 * Returns an empty [TrackerRepository] that is ready to be used in tests.
 */
fun emptyTrackerRepository(): TrackerRepository {
    val transactionTemplate = with(TVeebotApplication()) {
        transactionTemplate(transactionManager(dataSourceForDevelopment()))
    }

    return Transactional(ExposedTrackerRepository(transactionTemplate).apply {
        afterPropertiesSet()
    })
}
