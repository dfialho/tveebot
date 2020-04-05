package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class DatabaseTVeebotRepository(private val db: Database) : TVeebotRepository {

    private object TVShows : Table() {
        val ID = varchar("ID", length = 36).primaryKey()
        val TITLE = varchar("TITLE", length = 256)
        val TRACKED = bool("TRACKED").default(false)
        val VIDEO_QUALITY = enumeration("VIDEO_QUALITY", VideoQuality::class.java).default(VideoQuality.default())
    }

    private object Episodes : Table() {
        val TVSHOW_ID = reference("TVSHOW_ID", TVShows.ID).primaryKey()
        val SEASON = integer("SEASON").primaryKey()
        val NUMBER = integer("NUMBER").primaryKey()
        val TITLE = varchar("TITLE", length = 256)
    }

    private object Files : Table() {
        val LINK = varchar("LINK", length = 1024).primaryKey()
        val QUALITY = enumeration("QUALITY", VideoQuality::class.java).default(VideoQuality.default())
        val PUBLISHED_DATE = datetime("PUBLISHED_DATE")
    }

    private object EpisodeFiles : Table() {
        val EPISODE_TVSHOW_ID = reference("EPISODE_TVSHOW_ID", Episodes.TVSHOW_ID).primaryKey()
        val FILE_ID = reference("FILE_ID", Files.LINK).primaryKey()
    }

    init {
        transaction {
            SchemaUtils.create(TVShows, Episodes, Files, EpisodeFiles)
        }
    }

    override fun findTVShow(tvShowId: String, tracked: Boolean?): TVShowEntity? {

        return transaction {
            TVShows.select {
                if (tracked == null) {
                    (TVShows.ID eq tvShowId)
                } else {
                    (TVShows.ID eq tvShowId) and (TVShows.TRACKED eq tracked)
                }
            }.map {
                TVShowEntity(
                    TVShow(it[TVShows.ID], it[TVShows.TITLE]),
                    it[TVShows.TRACKED],
                    it[TVShows.VIDEO_QUALITY]
                )
            }.firstOrNull()
        }
    }

    override fun update(tvShow: TVShowEntity) {
        TODO("Not yet implemented")
    }

    override fun upsert(tvShow: TVShowEntity) {

        val alreadyExisted = TVShows.insertIgnore {
            it[ID] = tvShow.tvShow.id
            it[TITLE] = tvShow.tvShow.title
            it[TRACKED] = tvShow.tracked
            it[VIDEO_QUALITY] = tvShow.videoQuality
        }.isIgnore

        // FIXME
    }

    override fun insert(episodeFile: EpisodeFile) {
        transaction(db) {
            val fileId = insert(episodeFile.file)
            for (episode in episodeFile.episodes) {
                insert(episode)
                link(episode, fileId)
            }
        }
    }

    private fun insert(file: VideoFile): String {
        transaction(db) {
            Files.insertIgnore {
                it[LINK] = file.link
                it[QUALITY] = file.quality
                it[PUBLISHED_DATE] = DateTime(file.publishDate.toEpochMilli())
            }
        }

        return file.link
    }

    private fun insert(episode: Episode) {
        transaction(db) {
            Episodes.insertIgnore {
                it[TVSHOW_ID] = episode.tvShow.id
                it[SEASON] = episode.season
                it[NUMBER] = episode.number
                it[TITLE] = episode.title
            }
        }
    }

    private fun link(episode: Episode, fileId: String) {
        transaction(db) {
            EpisodeFiles.insertIgnore {
                it[EPISODE_TVSHOW_ID] = episode.tvShow.id
                it[FILE_ID] = fileId
            }
        }
    }

    override fun <T> transaction(block: TVeebotRepository.() -> T): T {
        return transaction(db) {
            this@DatabaseTVeebotRepository.block()
        }
    }
}