package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackerRepository
import dfialho.tveebot.tracker.api.toVideoQuality
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.replace
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
        val tracked = bool("tracked").default(false)
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

    override fun put(tvShow: TVShow, tracked: Boolean) {
        TVShows.insert {
            it[TVShows.id] = tvShow.id
            it[TVShows.title] = tvShow.title
            it[TVShows.tracked] = tracked
        }
    }

    override fun putAll(tvShows: List<TVShow>) {
        TVShows.batchInsert(tvShows, ignore = true) {
            this[TVShows.id] = it.id
            this[TVShows.title] = it.title
        }
    }

    override fun findAllTVShows(): List<TVShow> = TVShows
        .selectAll()
        .map {
            TVShow(
                title = it[TVShows.title],
                id = it[TVShows.id]
            )
        }

    override fun findTVShows(tracked: Boolean): List<TVShow> = TVShows
        .slice(TVShows.id, TVShows.title)
        .select { TVShows.tracked eq tracked }
        .map {
            TVShow(
                title = it[TVShows.title],
                id = it[TVShows.id]
            )
        }

    override fun setTracked(tvShowUUID: UUID, tracked: Boolean) {
        TVShows.update({ TVShows.id eq tvShowUUID }) {
            it[TVShows.tracked] = tracked
        }
    }

    override fun put(tvShow: TVShow, episodeFile: EpisodeFile) {
        EpisodeFiles.insert {
            it[EpisodeFiles.tvShowID] = tvShow.id
            it[EpisodeFiles.season] = episodeFile.episode.season
            it[EpisodeFiles.number] = episodeFile.episode.number
            it[EpisodeFiles.quality] = episodeFile.quality.identifier
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
}
