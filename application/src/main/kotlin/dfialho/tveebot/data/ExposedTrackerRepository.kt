package dfialho.tveebot.data

import dfialho.tveebot.application.api.EpisodeEntity
import dfialho.tveebot.application.api.EpisodeState
import dfialho.tveebot.application.api.TVShowEntity
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.tracker.api.models.*
import dfialho.tveebot.tracker.lib.EpisodeIDGenerator
import dfialho.tveebot.utils.Result
import mu.KLogging
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

/**
 * [TrackerRepository] implementation based on the exposed framework.
 *
 * FIXME
 * @author David Fialho (dfialho@protonmail.com)
 */
class ExposedTrackerRepository(private val db: Database) : TrackerRepository {

    companion object : KLogging()

    private object TVShows : Table() {
        val id = varchar("id", length = 36).primaryKey()
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
        val state = enumeration("state", EpisodeState::class.java).default(EpisodeState.AVAILABLE)
    }

    init {
        repositoryTransaction {
            SchemaUtils.create(TVShows, Episodes)
        }
    }

    override fun put(tvShow: TVShowEntity): Result = repositoryTransaction {
        TVShows.insertUnique {
            it[id] = tvShow.id.value
            it[title] = tvShow.title
            it[tracked] = tvShow.tracked
            it[quality] = tvShow.quality.toString()
        }
    }

    override fun putAll(tvShows: List<TVShowEntity>): Unit = repositoryTransaction {
        TVShows.batchInsert(tvShows, ignore = true) {
            this[TVShows.id] = it.id.value
            this[TVShows.title] = it.title
            this[TVShows.tracked] = it.tracked
            this[TVShows.quality] = it.quality.toString()
        }
    }

    override fun findTrackedTVShow(tvShowID: ID): TVShowEntity? = repositoryTransaction {
        TVShows
            .select { (TVShows.id eq tvShowID.value) and (TVShows.tracked eq true) }
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

    override fun setTracked(tvShowID: ID, quality: VideoQuality): Unit = repositoryTransaction {
        val updateCount = TVShows.update({ (TVShows.id eq tvShowID.value) and (TVShows.tracked eq false) }) {
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

    override fun setNotTracked(tvShowID: ID): Unit = repositoryTransaction {
        val updateCount = TVShows.update({ (TVShows.id eq tvShowID.value) and (TVShows.tracked eq true) }) {
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

    override fun setTVShowVideoQuality(tvShowID: ID, videoQuality: VideoQuality): Unit = repositoryTransaction {
        val updateCount = TVShows.update({ (TVShows.id eq tvShowID.value) and (TVShows.tracked eq true) }) {
            it[quality] = videoQuality.toString()
        }

        if (updateCount == 0) {
            throwTVShowNotFoundError(tvShowID, extraMessage = "it is not being tracked")
        }
    }

    override fun put(episodeFile: EpisodeFile): Result = repositoryTransaction {

        Episodes.insertUnique {
            it[id] = episodeFile.id
            it[tvShowID] = episodeFile.tvShow.id.value
            it[season] = episodeFile.episode.season
            it[number] = episodeFile.episode.number
            it[quality] = episodeFile.quality.toString()
            it[title] = episodeFile.episode.title
            it[link] = episodeFile.link
            it[publishDate] = DateTime(episodeFile.publishDate.toEpochMilli())
        }
    }

    override fun updateIf(
        episodeFile: EpisodeFile,
        predicate: (old: EpisodeFile, new: EpisodeFile) -> Boolean
    ): Result = repositoryTransaction {

        val existingEpisode = Episodes.innerJoin(TVShows, { tvShowID }, { id })
            .select { Episodes.id eq episodeFile.id }
            .map { it.toEpisodeFile() }
            .singleOrNull()

        if (existingEpisode != null && predicate(existingEpisode, episodeFile)) {
            Episodes.update({ Episodes.id eq episodeFile.id }) {
                it[title] = episodeFile.episode.title
                it[link] = episodeFile.link
                it[publishDate] = DateTime(episodeFile.publishDate.toEpochMilli())
            }

            Result.Success
        } else {
            Result.Failure
        }
    }

    override fun findEpisodesFrom(tvShowID: ID): List<EpisodeEntity> = repositoryTransaction {
        val episodes = Episodes
            .select { Episodes.tvShowID eq tvShowID.value }
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

    override fun removeEpisodesFrom(tvShowID: ID): Unit = repositoryTransaction {
        Episodes.deleteWhere { Episodes.tvShowID eq tvShowID.value }
    }

    override fun setEpisodeState(episodeFile: EpisodeFile, state: EpisodeState): Unit = repositoryTransaction {

        Episodes.update({ Episodes.id eq episodeFile.id }) {
            it[this.state] = state
        }

        logger.debug { "Updated state of episode '${episodeFile.toPrettyString()}' to '$state'" }
    }

    override fun clearAll(): Unit = repositoryTransaction {
        Episodes.deleteAll()
        TVShows.deleteAll()
    }

    /**
     * Checks if a TV show with [id] exists.
     */
    private fun tvShowExists(id: ID): Boolean {
        return TVShows.select { TVShows.id eq id.value }.count() > 0
    }

    private fun ResultRow.toTVShowEntity(): TVShowEntity =
        TVShowEntity(
            id = ID(this[TVShows.id]),
            title = this[TVShows.title],
            quality = this[TVShows.quality].toVideoQuality(),
            tracked = this[TVShows.tracked]
        )

    private fun ResultRow.toEpisodeEntity(): EpisodeEntity =
        EpisodeEntity(
            id = ID(this[Episodes.id]),
            title = this[Episodes.title],
            season = this[Episodes.season],
            number = this[Episodes.number],
            quality = this[Episodes.quality].toVideoQuality(),
            link = this[Episodes.link],
            publishDate = this[Episodes.publishDate].toDate().toInstant()
        )

    private fun ResultRow.toEpisodeFile(): EpisodeFile = EpisodeFile(
        episode = Episode(
            tvShow = TVShow(
                id = ID(this[TVShows.id]),
                title = this[TVShows.title]
            ),
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
}

private val EpisodeFile.id: String
    get() {
        return EpisodeIDGenerator.getID(episode.tvShow.id, episode.season, episode.number, quality).value
    }

private fun throwTVShowNotFoundError(id: ID, extraMessage: String? = null): Nothing {
    throw NoSuchElementException("TV Show '$id' not found" + if (extraMessage == null) "" else ": $extraMessage")
}
