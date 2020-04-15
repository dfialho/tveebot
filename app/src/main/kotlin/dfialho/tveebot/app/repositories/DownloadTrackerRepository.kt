package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.VideoFile
import dfialho.tveebot.app.components.DownloadTracker
import dfialho.tveebot.downloader.api.DownloadReference
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DownloadTrackerRepository(
    private val db: Database,
    private val repository: TVeebotRepository
) : DownloadTracker {

    init {
        transaction(db) {
            SchemaUtils.create(Downloads)
        }
    }

    override operator fun set(reference: DownloadReference, episodeFile: EpisodeFile) {

        transaction(db) {

            Downloads.insertIgnore {
                it[ID] = reference.value
                it[FILE_ID] = episodeFile.file.link
            }
        }
    }

    override operator fun get(reference: DownloadReference): EpisodeFile? {

        return transaction(db) {

            val videoFile = Downloads.innerJoin(Files)
                .select { Downloads.ID eq reference.value }
                .mapToFile()
                .firstOrNull()
                ?: return@transaction null

            EpisodeFile(
                videoFile,
                episodes = repository.findEpisodesByFile(videoFile.link)
                    .map { it.episode }
            )
        }
    }

    override fun list(): List<EpisodeFile> {

        return transaction(db) {

            val files = Downloads.innerJoin(Files)
                .selectAll()
                .mapToFile()

            return@transaction repository.transaction {
                files.map { file ->
                    EpisodeFile(
                        file,
                        findEpisodesByFile(file.link).map { it.episode }
                    )
                }
            }
        }
    }

    override fun remove(reference: DownloadReference) {

        transaction(db) {
            Downloads.deleteWhere { Downloads.ID eq reference.value }
        }
    }

    private fun Query.mapToFile(): List<VideoFile> = map {
        VideoFile(it[Files.LINK], it[Files.QUALITY], it[Files.PUBLISHED_DATE].toDate().toInstant())
    }
}
