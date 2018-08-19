package dfialho.tveebot.data

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.services.downloader.EpisodeDownload
import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.VideoQuality
import dfialho.tveebot.tracker.api.toVideoQuality
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import kotlin.NoSuchElementException

/**
 * [TrackerRepository] implementation based on the exposed framework.
 *
 * @param transactionTemplate Required to manage the transactions.
 * @author David Fialho (dfialho@protonmail.com)
 */
@Repository
@Transactional
class ExposedTrackerRepository(private val transactionTemplate: TransactionTemplate)
    : TrackerRepository, InitializingBean {

    private object TVShows : Table() {
        val id = uuid("id").primaryKey()
        val title = varchar("title", length = 256)
        val tracked = bool("tracked")
        val quality = varchar("quality", length = 32)
    }

    private object Episodes : Table() {
        val id = varchar("id", length = 128).primaryKey()
        val tvShowID = reference("tvshow_id", TVShows.id)
        val season = integer("season")
        val number = integer("number")
        val quality = varchar("quality", length = 32)
        val title = varchar("title", length = 256)
        val link = varchar("link", length = 1024)
        val publishedDate = datetime("published_date")
    }

    private object Downloads : Table() {
        val reference = varchar("reference", length = 256).primaryKey()
        val episodeID = reference("episode_id", Episodes.id)
        val tvShowID = reference("tvshow_id", Episodes.tvShowID)
    }

    override fun afterPropertiesSet() {
        transactionTemplate.execute {
            SchemaUtils.create(TVShows)
            SchemaUtils.create(Episodes)
            SchemaUtils.create(Downloads)
        }
    }

    override fun put(tvShow: TVShow): Unit = repositoryTransaction {
        TVShows.insert {
            it[id] = tvShow.id
            it[title] = tvShow.title
            it[tracked] = tvShow.tracked
            it[quality] = tvShow.quality.toString()
        }
    }

    override fun putAll(tvShows: List<TVShow>): Unit = repositoryTransaction {
        TVShows.batchInsert(tvShows, ignore = true) {
            this[TVShows.id] = it.id
            this[TVShows.title] = it.title
            this[TVShows.tracked] = it.tracked
            this[TVShows.quality] = it.quality.toString()
        }
    }

    override fun findTrackedTVShow(tvShowUUID: UUID): TVShow? = repositoryTransaction {
        TVShows
            .select { (TVShows.id eq tvShowUUID) and (TVShows.tracked eq true) }
            .map { it.toTVShow() }
            .singleOrNull()
    }

    override fun findAllTVShows(): List<TVShow> = repositoryTransaction {
        TVShows
            .selectAll()
            .map { it.toTVShow() }
    }

    override fun findTrackedTVShows(): List<TVShow> = repositoryTransaction {
        TVShows
            .select { TVShows.tracked eq true }
            .map { it.toTVShow() }
    }

    override fun findNotTrackedTVShows(): List<TVShow> = repositoryTransaction {
        TVShows
            .select { TVShows.tracked eq false }
            .map { it.toTVShow() }
    }

    override fun setTracked(tvShowUUID: UUID, quality: VideoQuality): Unit = repositoryTransaction {
        TVShows.update({ TVShows.id eq tvShowUUID }) {
            it[tracked] = true
            it[this.quality] = quality.toString()
        }
    }

    override fun setNotTracked(tvShowUUID: UUID): Unit = repositoryTransaction {
        TVShows.update({ TVShows.id eq tvShowUUID }) {
            it[tracked] = false
        }
    }

    override fun setTVShowVideoQuality(tvShowUUID: UUID, videoQuality: VideoQuality): Unit = repositoryTransaction {
        val updateCount = TVShows.update({ (TVShows.id eq tvShowUUID) and (TVShows.tracked eq true) }) {
            it[quality] = videoQuality.toString()
        }

        if (updateCount == 0) {
            throw TrackerRepositoryException("No TV show with ID '$tvShowUUID' is being tracked")
        }
    }

    override fun put(tvShowUUID: UUID, episode: EpisodeFile): Unit = repositoryTransaction {
        Episodes.insert {
            it[id] = episodeIDOf(tvShowUUID, episode)
            it[tvShowID] = tvShowUUID
            it[season] = episode.season
            it[number] = episode.number
            it[quality] = episode.quality.toString()
            it[title] = episode.title
            it[link] = episode.link
            it[publishedDate] = DateTime(episode.publishedDate.toEpochMilli())
        }
    }

    override fun putOrUpdateIf(
        tvShowUUID: UUID,
        episode: EpisodeFile,
        predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean
    ): Boolean = repositoryTransaction {

        val existingEpisode = Episodes
            .select { Episodes.id eq episodeIDOf(tvShowUUID, episode) }
            .map { it.toEpisodeFile() }
            .singleOrNull()

        when {
            existingEpisode == null -> {
                put(tvShowUUID, episode)
                true
            }
            predicate(existingEpisode, episode) -> {
                Episodes.update({ Episodes.id eq episodeIDOf(tvShowUUID, episode) }) {
                    it[Episodes.title] = episode.title
                    it[Episodes.link] = episode.link
                    it[Episodes.publishedDate] = DateTime(episode.publishedDate.toEpochMilli())
                }
                true
            }
            else -> false
        }
    }

    override fun findEpisodesFrom(tvShowUUID: UUID): List<EpisodeFile> = repositoryTransaction {
        val episodes = Episodes
            .select { Episodes.tvShowID eq tvShowUUID }
            .map { it.toEpisodeFile() }

        if (episodes.isEmpty() && !tvShowExists(tvShowUUID)) {
            throw NoSuchElementException("TV show `$tvShowUUID` not found")
        }

        episodes
    }

    override fun findEpisodesByTVShow(): Map<TVShow, List<EpisodeFile>> = repositoryTransaction {
        val episodesByTVShow = mutableMapOf<TVShow, MutableList<EpisodeFile>>()

        (TVShows innerJoin Episodes)
            .selectAll()
            .map { it.toTVShow() to it.toEpisodeFile() }
            .forEach { (tvShow, episode) -> episodesByTVShow.putIfAbsent(tvShow, mutableListOf(episode))?.add(episode) }

        episodesByTVShow
    }

    override fun removeEpisodesFrom(tvShowUUID: UUID): Unit = repositoryTransaction {
        Downloads.deleteWhere { Downloads.tvShowID eq tvShowUUID }
        Episodes.deleteWhere { Episodes.tvShowID eq tvShowUUID }
    }

    override fun put(download: EpisodeDownload): Unit = repositoryTransaction {
        Downloads.insert {
            it[episodeID] = episodeIDOf(download.tvShow.id, download.episode)
            it[tvShowID] = download.tvShow.id
            it[reference] = download.reference.value
        }
    }

    override fun findDownload(reference: DownloadReference): EpisodeDownload? = repositoryTransaction {
        (Downloads innerJoin Episodes innerJoin TVShows)
            .select { Downloads.reference eq reference.value }
            .map { it.toEpisodeDownload() }
            .singleOrNull()
    }

    override fun findAllDownloads(): List<EpisodeDownload> = repositoryTransaction {
        (Downloads innerJoin Episodes innerJoin TVShows)
            .selectAll()
            .map { it.toEpisodeDownload() }
    }

    override fun findDownloadsFrom(tvShowUUID: UUID): List<EpisodeDownload> = repositoryTransaction {
        (Downloads innerJoin Episodes innerJoin TVShows)
            .select { (Downloads.tvShowID eq tvShowUUID) }
            .map { it.toEpisodeDownload() }
    }

    override fun removeDownload(reference: DownloadReference): Unit = repositoryTransaction {
        Downloads.deleteWhere { Downloads.reference eq reference.value }
    }

    override fun removeAllDownloadsFrom(tvShowUUID: UUID): Unit = repositoryTransaction {
        Downloads.deleteWhere { Downloads.tvShowID eq tvShowUUID }
    }

    override fun clearAll(): Unit = repositoryTransaction {
        Downloads.deleteAll()
        Episodes.deleteAll()
        TVShows.deleteAll()
    }

    /**
     * Checks if a TV show with [uuid] exists.
     */
    private fun tvShowExists(uuid: UUID): Boolean {
        return TVShows.select { TVShows.id eq uuid }.count() > 0
    }

    private fun ResultRow.toTVShow(): TVShow = TVShow(
        id = this[TVShows.id],
        title = this[TVShows.title],
        quality = this[TVShows.quality].toVideoQuality(),
        tracked = this[TVShows.tracked]
    )

    private fun ResultRow.toEpisodeDownload(): EpisodeDownload = EpisodeDownload(
        reference = DownloadReference(this[Downloads.reference]),
        tvShow = this.toTVShow(),
        episode = this.toEpisodeFile()
    )

    private fun ResultRow.toEpisodeFile(): EpisodeFile = EpisodeFile(
        episode = Episode(
            title = this[Episodes.title],
            season = this[Episodes.season],
            number = this[Episodes.number]
        ),
        quality = this[Episodes.quality].toVideoQuality(),
        link = this[Episodes.link],
        publishedDate = this[Episodes.publishedDate].toDate().toInstant()
    )

    private fun <T> repositoryTransaction(body: () -> T): T {
        try {
            return body()
        } catch (e: ExposedSQLException) {
            throw TrackerRepositoryException("Failed to execute operation", e)
        }
    }

    private fun episodeIDOf(tvShowUUID: UUID, episode: EpisodeFile): String {
        return "$tvShowUUID.${episode.season}.${episode.number}.${episode.quality}"
    }
}
