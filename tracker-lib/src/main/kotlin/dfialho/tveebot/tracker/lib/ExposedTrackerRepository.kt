package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackedTVShow
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.api.VideoQuality
import dfialho.tveebot.tracker.api.toVideoQuality
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
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

    private object EpisodeFiles : Table() {
        val tvShowID = uuid("tvshow_id").primaryKey()
        val season = integer("season").primaryKey()
        val number = integer("number").primaryKey()
        val quality = varchar("quality", length = 32).primaryKey()
        val title = varchar("title", length = 256)
        val link = varchar("link", length = 1024)
        val publishedDate = datetime("published_date")
    }

    override fun afterPropertiesSet() {
        transactionTemplate.execute {
            SchemaUtils.create(TVShows)
            SchemaUtils.create(EpisodeFiles)
        }
    }

    override fun putAll(tvShows: List<TVShow>) {
        TVShows.batchInsert(tvShows, ignore = true) {
            this[TVShows.id] = it.id
            this[TVShows.title] = it.title
        }
    }

    override fun findTrackedTVShow(tvShowUUID: UUID): TrackedTVShow? = TVShows
        .select { TVShows.id eq tvShowUUID }
        .map { it.toTrackedTVShow() }
        .firstOrNull()

    private fun ResultRow.toTrackedTVShow(): TrackedTVShow = TrackedTVShow(
        TVShow(
            title = this[TVShows.title],
            id = this[TVShows.id]
        ),
        quality = this[TVShows.quality].toVideoQuality()
    )

    override fun findAllTVShows(): List<TVShow> = TVShows
        .selectAll()
        .map {
            TVShow(
                title = it[TVShows.title],
                id = it[TVShows.id]
            )
        }

    override fun findTrackedTVShows(): List<TrackedTVShow> = TVShows
        .select { TVShows.tracked eq true }
        .map { it.toTrackedTVShow() }

    override fun findNotTrackedTVShows(): List<TVShow> = TVShows
        .select { TVShows.tracked eq false }
        .map {
            TVShow(
                title = it[TVShows.title],
                id = it[TVShows.id]
            )
        }

    override fun setTracked(tvShowUUID: UUID, quality: VideoQuality) {
        TVShows.update({ TVShows.id eq tvShowUUID }) {
            it[TVShows.tracked] = true
            it[TVShows.quality] = quality.toString()
        }
    }

    override fun setNotTracked(tvShowUUID: UUID) {
        TVShows.update({ TVShows.id eq tvShowUUID }) {
            it[TVShows.tracked] = false
        }
    }

    override fun setTVShowVideoQuality(tvShowUUID: UUID, videoQuality: VideoQuality) {
        TVShows.update({ TVShows.id eq tvShowUUID }) {
            it[TVShows.quality] = videoQuality.toString()
        }
    }

    override fun put(tvShowUUID: UUID, episodeFile: EpisodeFile) {
        EpisodeFiles.insert {
            it[EpisodeFiles.tvShowID] = tvShowUUID
            it[EpisodeFiles.season] = episodeFile.episode.season
            it[EpisodeFiles.number] = episodeFile.episode.number
            it[EpisodeFiles.quality] = episodeFile.quality.toString()
            it[EpisodeFiles.title] = episodeFile.episode.title
            it[EpisodeFiles.link] = episodeFile.link
            it[EpisodeFiles.publishedDate] = DateTime(episodeFile.publishedDate.toEpochMilli())
        }
    }

    override fun findEpisodeFilesFrom(tvShow: TVShow): List<EpisodeFile> = EpisodeFiles
        .select { EpisodeFiles.tvShowID eq tvShow.id }
        .map {
            EpisodeFile(
                episode = Episode(
                    title = it[EpisodeFiles.title],
                    season = it[EpisodeFiles.season],
                    number = it[EpisodeFiles.number]
                ),
                quality = it[EpisodeFiles.quality].toVideoQuality(),
                link = it[EpisodeFiles.link],
                publishedDate = it[EpisodeFiles.publishedDate].toDate().toInstant()
            )
        }

    override fun removeEpisodeFilesFrom(tvShowUUID: UUID) {
        EpisodeFiles.deleteWhere { EpisodeFiles.tvShowID eq tvShowUUID }
    }
}
