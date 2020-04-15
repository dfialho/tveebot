package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class DatabaseTVeebotRepository(private val db: Database) : TVeebotRepository {

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

    override fun findTVShows(tracked: Boolean?): List<TVShowEntity> {

        return transaction {

            if (tracked != null) {
                TVShows.select { TVShows.TRACKED eq tracked }
            } else {
                TVShows.selectAll()
            }.map {
                TVShowEntity(
                    TVShow(it[TVShows.ID], it[TVShows.TITLE]),
                    it[TVShows.TRACKED],
                    it[TVShows.VIDEO_QUALITY]
                )
            }
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
                TVShows.update({ TVShows.ID eq tvShow.tvShow.id }) {
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

    override fun findEpisodeLatestFile(episodeId: String, quality: VideoQuality): VideoFile? {

        return EpisodeFiles.innerJoin(Files)
            .select { (EpisodeFiles.EPISODE_ID eq episodeId) and (Files.QUALITY eq quality) }
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
                .limit(1)
                .map {
                    EpisodeEntity(
                        Episode(
                            TVShow(
                                it[TVShows.ID],
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

    override fun findEpisodeFile(fileId: String): EpisodeFile? {

        return transaction {

            (Episodes innerJoin TVShows)
                .join(EpisodeFiles.innerJoin(Files), JoinType.INNER, Episodes.ID, EpisodeFiles.EPISODE_ID)
                .select { Files.LINK eq fileId }
                .limit(1)
                .map {
                    EpisodeFile(
                        VideoFile(
                            it[Files.LINK],
                            it[Files.QUALITY],
                            it[Files.PUBLISHED_DATE].toDate().toInstant()
                        ),
                        listOf(
                            Episode(
                                TVShow(it[TVShows.ID], it[TVShows.TITLE]),
                                it[Episodes.SEASON],
                                it[Episodes.NUMBER],
                                it[Episodes.TITLE]
                            )
                        )
                    )
                }
                .groupBy { it.file }
                .values
                .map {
                    it[0].copy(episodes = it.map { it.episodes[0] })
                }
                .firstOrNull()
        }
    }

    override fun findEpisodes(): List<EpisodeEntity> {

        return transaction {
            Episodes.innerJoin(TVShows)
                .selectAll()
                .map {
                    EpisodeEntity(
                        Episode(
                            TVShow(
                                it[TVShows.ID],
                                it[TVShows.TITLE]
                            ),
                            it[Episodes.SEASON],
                            it[Episodes.NUMBER],
                            it[Episodes.TITLE]
                        ),
                        it[Episodes.STATE]
                    )
                }
        }
    }

    override fun findEpisodeFiles(
        tvShowId: String,
        state: State,
        videoQuality: VideoQuality
    ): List<EpisodeFile> {

        return transaction {

            (Episodes innerJoin TVShows)
                .join(EpisodeFiles.innerJoin(Files), JoinType.INNER, Episodes.ID, EpisodeFiles.EPISODE_ID)
                .select { (TVShows.ID eq tvShowId) and (Episodes.STATE eq state) and (Files.QUALITY eq videoQuality) }
                .map {
                    EpisodeFile(
                        VideoFile(
                            it[Files.LINK],
                            it[Files.QUALITY],
                            it[Files.PUBLISHED_DATE].toDate().toInstant()
                        ),
                        listOf(
                            Episode(
                                TVShow(it[TVShows.ID], it[TVShows.TITLE]),
                                it[Episodes.SEASON],
                                it[Episodes.NUMBER],
                                it[Episodes.TITLE]
                            )
                        )
                    )
                }
                .groupBy { it.file }
                .values
                .map {
                    it[0].copy(episodes = it.map { it.episodes[0] })
                }
        }
    }

    override fun findEpisodesByFile(fileId: String): List<EpisodeEntity> {

        return transaction {

            Episodes.innerJoin(TVShows).innerJoin(EpisodeFiles)
                .select { EpisodeFiles.FILE_ID eq fileId }
                .map {
                    EpisodeEntity(
                        Episode(
                            TVShow(it[TVShows.ID], it[TVShows.TITLE]),
                            it[Episodes.SEASON],
                            it[Episodes.NUMBER],
                            it[Episodes.TITLE]
                        ),
                        it[Episodes.STATE]
                    )
                }
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