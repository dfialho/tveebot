package dfialho.tveebot.data

import dfialho.tveebot.data.models.EpisodeDownload
import dfialho.tveebot.data.models.EpisodeEntity
import dfialho.tveebot.data.models.TVShowEntity
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.toEpisodeFile
import dfialho.tveebot.tracker.api.models.Episode
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.TVShowID
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tracker.api.models.toVideoQuality
import dfialho.tveebot.tracker.lib.EpisodeIDGenerator
import dfialho.tveebot.tvShowEpisodeFileOf
import dfialho.tveebot.utils.Result
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
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
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import java.util.*
import kotlin.NoSuchElementException

/**
 * [TrackerRepository] implementation based on the exposed framework.
 *
 * FIXME
 * @author David Fialho (dfialho@protonmail.com)
 */
class ExposedTrackerRepository(private val db: Database) : TrackerRepository {

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
        val publishDate = datetime("published_date")
    }

    private object Downloads : Table() {
        val reference = varchar("reference", length = 256).primaryKey()
        val episodeID = reference("episode_id", Episodes.id)
        val tvShowID = reference("tvshow_id", Episodes.tvShowID)
    }

    init {
        repositoryTransaction {
            SchemaUtils.create(TVShows, Episodes, Downloads)
        }
    }

    override fun put(tvShow: TVShowEntity): Result = repositoryTransaction {
        TVShows.insertUnique {
            it[id] = tvShow.id
            it[title] = tvShow.title
            it[tracked] = tvShow.tracked
            it[quality] = tvShow.quality.toString()
        }
    }

    override fun putAll(tvShows: List<TVShowEntity>): Unit = repositoryTransaction {
        TVShows.batchInsert(tvShows, ignore = true) {
            this[TVShows.id] = it.id
            this[TVShows.title] = it.title
            this[TVShows.tracked] = it.tracked
            this[TVShows.quality] = it.quality.toString()
        }
    }

    override fun findTrackedTVShow(tvShowID: TVShowID): TVShowEntity? = repositoryTransaction {
        TVShows
            .select { (TVShows.id eq tvShowID) and (TVShows.tracked eq true) }
            .map { it.toTVShowEntity() }
            .singleOrNull()
    }

    override fun findAllTVShows(): List<TVShowEntity> = repositoryTransaction {
        TVShows
            .selectAll()
            .map { it.toTVShowEntity() }
    }

    override fun findTrackedTVShows(): List<TVShowEntity> = repositoryTransaction {
        TVShows
            .select { TVShows.tracked eq true }
            .map { it.toTVShowEntity() }
    }

    override fun findNotTrackedTVShows(): List<TVShowEntity> = repositoryTransaction {
        TVShows
            .select { TVShows.tracked eq false }
            .map { it.toTVShowEntity() }
    }

    override fun setTracked(tvShowID: TVShowID, quality: VideoQuality): Unit = repositoryTransaction {
        val updateCount = TVShows.update({ (TVShows.id eq tvShowID) and (TVShows.tracked eq false) }) {
            it[tracked] = true
            it[this.quality] = quality.toString()
        }

        if (updateCount == 0) {
            if (tvShowExists(tvShowID)) {
                throw IllegalStateException("TV Show '$tvShowID' is already being tracked")
            } else {
                throwTVShowNotFoundError(tvShowID)
            }
        }
    }

    override fun setNotTracked(tvShowID: TVShowID): Unit = repositoryTransaction {
        val updateCount = TVShows.update({ (TVShows.id eq tvShowID) and (TVShows.tracked eq true) }) {
            it[tracked] = false
        }

        if (updateCount == 0) {
            if (tvShowExists(tvShowID)) {
                throw IllegalStateException("TV Show '$tvShowID' is not being tracked")
            } else {
                throwTVShowNotFoundError(tvShowID)
            }
        }
    }

    override fun setTVShowVideoQuality(tvShowID: TVShowID, videoQuality: VideoQuality): Unit = repositoryTransaction {
        val updateCount = TVShows.update({ (TVShows.id eq tvShowID) and (TVShows.tracked eq true) }) {
            it[quality] = videoQuality.toString()
        }

        if (updateCount == 0) {
            throwTVShowNotFoundError(tvShowID, extraMessage = "it is not being tracked")
        }
    }

    override fun put(tvShowID: TVShowID, episode: EpisodeFile): Result = repositoryTransaction {
        Episodes.insertUnique {
            it[id] = episodeIDOf(tvShowID, episode)
            it[this.tvShowID] = tvShowID
            it[season] = episode.season
            it[number] = episode.number
            it[quality] = episode.quality.toString()
            it[title] = episode.title
            it[link] = episode.link
            it[publishDate] = DateTime(episode.publishDate.toEpochMilli())
        }
    }

    override fun put(episode: TVShowEpisodeFile): Result = put(episode.tvShowID, episode.toEpisodeFile())

    override fun updateIf(
        episode: TVShowEpisodeFile,
        predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean
    ): Result = repositoryTransaction {

        val existingEpisode = Episodes
            .select { Episodes.id eq episodeIDOf(episode) }
            .map { it.toEpisodeFile() }
            .singleOrNull()

        if (existingEpisode != null && predicate(existingEpisode, episode.toEpisodeFile())) {
            Episodes.update({ Episodes.id eq episodeIDOf(episode) }) {
                it[Episodes.title] = episode.title
                it[Episodes.link] = episode.link
                it[Episodes.publishDate] = DateTime(episode.publishDate.toEpochMilli())
            }

            Result.Success
        } else {
            Result.Failure
        }
    }

    override fun putOrUpdateIf(
        tvShowID: TVShowID,
        episode: EpisodeFile,
        predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean
    ): Boolean = repositoryTransaction {

        val existingEpisode = Episodes
            .select { Episodes.id eq episodeIDOf(tvShowID, episode) }
            .map { it.toEpisodeFile() }
            .singleOrNull()

        when {
            existingEpisode == null -> {
                put(tvShowID, episode)
                true
            }
            predicate(existingEpisode, episode) -> {
                Episodes.update({ Episodes.id eq episodeIDOf(tvShowID, episode) }) {
                    it[Episodes.title] = episode.title
                    it[Episodes.link] = episode.link
                    it[Episodes.publishDate] = DateTime(episode.publishDate.toEpochMilli())
                }
                true
            }
            else -> false
        }
    }

    override fun findEpisodesFrom(tvShowID: TVShowID): List<EpisodeEntity> = repositoryTransaction {
        val episodes = Episodes
            .select { Episodes.tvShowID eq tvShowID }
            .map { it.toEpisodeEntity() }

        if (episodes.isEmpty() && !tvShowExists(tvShowID)) {
            throw NoSuchElementException("TV show `$tvShowID` not found")
        }

        episodes
    }

    override fun findEpisodesByTVShow(): Map<TVShowEntity, List<EpisodeEntity>> = repositoryTransaction {
        val episodesByTVShow = mutableMapOf<TVShowEntity, MutableList<EpisodeEntity>>()

        (TVShows innerJoin Episodes)
            .selectAll()
            .map { it.toTVShowEntity() to it.toEpisodeEntity() }
            .forEach { (tvShow, episode) -> episodesByTVShow.putIfAbsent(tvShow, mutableListOf(episode))?.add(episode) }

        episodesByTVShow
    }

    override fun removeEpisodesFrom(tvShowID: TVShowID): Unit = repositoryTransaction {
        Downloads.deleteWhere { Downloads.tvShowID eq tvShowID }
        Episodes.deleteWhere { Episodes.tvShowID eq tvShowID }
    }

    override fun put(download: EpisodeDownload): Unit = repositoryTransaction {
        Downloads.insert {
            it[episodeID] = episodeIDOf(download.episode)
            it[tvShowID] = download.episode.tvShowID
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

    override fun findDownloadsFrom(tvShowID: TVShowID): List<EpisodeDownload> = repositoryTransaction {
        (Downloads innerJoin Episodes innerJoin TVShows)
            .select { (Downloads.tvShowID eq tvShowID) }
            .map { it.toEpisodeDownload() }
    }

    override fun removeDownload(reference: DownloadReference): Unit = repositoryTransaction {
        Downloads.deleteWhere { Downloads.reference eq reference.value }
    }

    override fun removeAllDownloads(references: List<DownloadReference>): Unit = repositoryTransaction {
        Downloads.deleteWhere { Downloads.reference inList references.map { it.value } }
    }

    override fun removeAllDownloadsFrom(tvShowID: TVShowID): Unit = repositoryTransaction {
        Downloads.deleteWhere { Downloads.tvShowID eq tvShowID }
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

    private fun ResultRow.toTVShowEntity(): TVShowEntity = TVShowEntity(
        id = this[TVShows.id],
        title = this[TVShows.title],
        quality = this[TVShows.quality].toVideoQuality(),
        tracked = this[TVShows.tracked]
    )

    private fun ResultRow.toTVShow(): TVShow = TVShow(
        id = this[TVShows.id],
        title = this[TVShows.title],
        quality = this[TVShows.quality].toVideoQuality()
    )

    private fun ResultRow.toEpisodeDownload(): EpisodeDownload = EpisodeDownload(
        reference = DownloadReference(this[Downloads.reference]),
        episode = tvShowEpisodeFileOf(
            tvShow = this.toTVShow(),
            episode = this.toEpisodeFile()
        )
    )
    private fun ResultRow.toEpisodeEntity(): EpisodeEntity = EpisodeEntity(
        id = this[Episodes.id],
        title = this[Episodes.title],
        season = this[Episodes.season],
        number = this[Episodes.number],
        quality = this[Episodes.quality].toVideoQuality(),
        link = this[Episodes.link],
        publishDate = this[Episodes.publishDate].toDate().toInstant()
    )

    private fun ResultRow.toEpisodeFile(): EpisodeFile = EpisodeFile(
        episode = Episode(
            title = this[Episodes.title],
            season = this[Episodes.season],
            number = this[Episodes.number]
        ),
        quality = this[Episodes.quality].toVideoQuality(),
        link = this[Episodes.link],
        publishDate = this[Episodes.publishDate].toDate().toInstant()
    )

    private fun <T> repositoryTransaction(body: () -> T): T {
        try {
            return transaction(db) { body() }
        } catch (e: ExposedSQLException) {
            throw TrackerRepositoryException("Failed to execute operation", e.cause)
        }
    }

    private fun episodeIDOf(tvShowID: TVShowID, episode: EpisodeFile): String {
        return with(episode) {
            EpisodeIDGenerator.getID(tvShowID, season, number, quality)
        }
    }

    private fun episodeIDOf(episode: TVShowEpisodeFile): String {
        return with(episode) {
            EpisodeIDGenerator.getID(tvShowID, season, number, quality)
        }
    }
}

private fun throwTVShowNotFoundError(uuid: UUID, extraMessage: String? = null): Nothing {
    throw NoSuchElementException("TV Show '$uuid' not found" + if (extraMessage == null) "" else ": $extraMessage")
}
