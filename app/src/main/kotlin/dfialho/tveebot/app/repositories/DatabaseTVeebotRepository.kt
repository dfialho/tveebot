package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.*
import dfialho.tveebot.app.repositories.DatabaseTVeebotRepository.TVShows.ID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class DatabaseTVeebotRepository(private val db: Database) : TVeebotRepository {

    private object TVShows : Table() {
        val ID = varchar("ID", length = 256).primaryKey()
        val TITLE = varchar("TITLE", length = 256)
        val TRACKED = bool("TRACKED").default(false)
        val VIDEO_QUALITY = enumeration("VIDEO_QUALITY", VideoQuality::class.java).default(VideoQuality.default())
    }

    private object Episodes : Table() {
        val ID = varchar("ID", length = 256).primaryKey()
        val TVSHOW_ID = reference("TVSHOW_ID", TVShows.ID)
        val SEASON = integer("SEASON")
        val NUMBER = integer("NUMBER")
        val TITLE = varchar("TITLE", length = 256)
        val STATE = enumeration("STATE", State::class.java).default(State.FOUND)
    }

    private object Files : Table() {
        val LINK = varchar("LINK", length = 2048).primaryKey()
        val QUALITY = enumeration("QUALITY", VideoQuality::class.java).default(VideoQuality.default())
        val PUBLISHED_DATE = datetime("PUBLISHED_DATE")
    }

    private object EpisodeFiles : Table() {
        val EPISODE_ID = reference("EPISODE_ID", Episodes.ID).primaryKey()
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
                    (ID eq tvShowId)
                } else {
                    (ID eq tvShowId) and (TVShows.TRACKED eq tracked)
                }
            }.map {
                TVShowEntity(
                    TVShow(it[ID], it[TVShows.TITLE]),
                    it[TVShows.TRACKED],
                    it[TVShows.VIDEO_QUALITY]
                )
            }.firstOrNull()
        }
    }

    override fun upsert(tvShow: TVShowEntity) {

        transaction {
            val alreadyExists = TVShows.insertIgnore {
                it[ID] = tvShow.tvShow.id
                it[TITLE] = tvShow.tvShow.title
                it[TRACKED] = tvShow.tracked
                it[VIDEO_QUALITY] = tvShow.videoQuality
            }.isIgnore


            if (alreadyExists) {
                TVShows.update({ ID eq tvShow.tvShow.id }) {
                    it[TITLE] = tvShow.tvShow.title
                    it[TRACKED] = tvShow.tracked
                    it[VIDEO_QUALITY] = tvShow.videoQuality
                }
            }
        }
    }

    override fun update(episode: EpisodeEntity) {

        transaction {
            Episodes.update({ Episodes.ID eq episode.episode.id }) {
                it[TVSHOW_ID] = episode.episode.tvShow.id
                it[SEASON] = episode.episode.season
                it[NUMBER] = episode.episode.number
                it[TITLE] = episode.episode.title
                it[STATE] = episode.state
            }
        }
    }

    override fun findEpisodeLatestFile(id: String): VideoFile? {

        return EpisodeFiles.innerJoin(Files)
            .select { EpisodeFiles.EPISODE_ID eq id }
            .map {
                VideoFile(
                    it[Files.LINK],
                    it[Files.QUALITY],
                    it[Files.PUBLISHED_DATE].toDate().toInstant()
                )
            }.firstOrNull()
    }

    override fun findEpisode(id: String): EpisodeEntity? {

        return transaction {
            Episodes.innerJoin(TVShows)
                .select { Episodes.ID eq id }
                .map {
                    EpisodeEntity(
                        Episode(
                            TVShow(
                                it[ID],
                                it[TVShows.TITLE]
                            ),
                            it[Episodes.SEASON],
                            it[Episodes.NUMBER],
                            it[Episodes.TITLE]
                        ),
                        it[Episodes.STATE]
                    )
                }
                .firstOrNull()
        }
    }

    override fun insert(episodeFile: EpisodeFile) {
        transaction {
            val fileId = insert(episodeFile.file)
            for (episode in episodeFile.episodes) {
                insert(episode)
                link(episode, fileId)
            }
        }
    }

    private fun insert(file: VideoFile): String {
        transaction {
            Files.insertIgnore {
                it[LINK] = file.link
                it[QUALITY] = file.quality
                it[PUBLISHED_DATE] = DateTime(file.publishDate.toEpochMilli())
            }
        }

        return file.link
    }

    private fun insert(episode: Episode) {
        transaction {
            Episodes.insertIgnore {
                it[ID] = episode.id
                it[TVSHOW_ID] = episode.tvShow.id
                it[SEASON] = episode.season
                it[NUMBER] = episode.number
                it[TITLE] = episode.title
            }
        }
    }

    private fun link(episode: Episode, fileId: String) {
        transaction {
            EpisodeFiles.insertIgnore {
                it[EPISODE_ID] = episode.id
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