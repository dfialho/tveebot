package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.toVideoQuality
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.joda.time.DateTime
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

/**
 * Implementation of a [EpisodeDownloadQueue] which persists the downloads in a database. This component is used by the
 * downloader service to resume the downloads from a previous session when the application is restarted.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@Component
@Transactional
class ExposedEpisodeDownloadQueue(private val transactionTemplate: TransactionTemplate) : EpisodeDownloadQueue, InitializingBean {

    private object Downloads : Table() {
        val reference = varchar("reference", length = 256).primaryKey()
        val tvShowID = uuid("tvshow_id")
        val season = integer("season")
        val number = integer("number")
        val title = varchar("title", length = 256)
        val quality = varchar("quality", length = 32)
        val link = varchar("link", length = 1024)
        val publishedDate = datetime("published_date")
    }

    override fun afterPropertiesSet() {
        transactionTemplate.execute {
            SchemaUtils.create(Downloads)
        }
    }

    override fun push(episodeDownload: EpisodeDownload) {
        Downloads.insertIgnore {
            it[Downloads.reference] = episodeDownload.reference.value
            it[Downloads.tvShowID] = episodeDownload.tvShowUUID
            it[Downloads.season] = episodeDownload.episodeFile.episode.season
            it[Downloads.number] = episodeDownload.episodeFile.episode.number
            it[Downloads.title] = episodeDownload.episodeFile.episode.title
            it[Downloads.quality] = episodeDownload.episodeFile.quality.toString()
            it[Downloads.link] = episodeDownload.episodeFile.link
            it[Downloads.publishedDate] = DateTime(episodeDownload.episodeFile.publishedDate.toEpochMilli())
        }
    }

    override fun getAll(): List<EpisodeDownload> = Downloads
        .selectAll()
        .map {
            EpisodeDownload(
                reference = DownloadReference(it[Downloads.reference]),
                tvShowUUID = it[Downloads.tvShowID],
                episodeFile = EpisodeFile(
                    episode = Episode(
                        title = it[Downloads.title],
                        season = it[Downloads.season],
                        number = it[Downloads.number]
                    ),
                    quality = it[Downloads.quality].toVideoQuality(),
                    link = it[Downloads.link],
                    publishedDate = it[Downloads.publishedDate].toDate().toInstant()
                )
            )
        }

    override fun remove(reference: DownloadReference) {
        Downloads.deleteWhere { Downloads.reference eq reference.value }
    }
}