package dfialho.tveebot.data.migrator

import dfialho.tveebot.application.api.EpisodeState
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Paths

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

private object Downloads : Table() {
    val reference = varchar("reference", length = 256).primaryKey()
    val episodeID = reference("episode_id", Episodes.id)
    val tvShowID = reference("tvshow_id", Episodes.tvShowID)
}

const val DB_EXTENSION = ".mv.db"

fun main(args: Array<String>) {

    if (args.size != 1) {
        System.err.println("Usage: migrator <repository-path>")
        System.exit(1)
    }

    val dbPath = Paths.get(args[0])

    if (Files.exists(dbPath)) {
        System.err.println("Repository path not found: $dbPath")
        System.exit(1)
    }

    val db = Database.connect(
        url = "jdbc:h2:${removeDbExtension(dbPath.toString())};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE",
        driver = "org.h2.Driver"
    )

    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(TVShows, Episodes)

        val downloadingEpisodeIDs = Downloads.selectAll().map { it[Downloads.episodeID] }

        val storedEpisodesIDs = Episodes.innerJoin(TVShows, { tvShowID }, { id })
            .select {
                (TVShows.tracked eq true)
                    .and(Episodes.id notInList downloadingEpisodeIDs)
                    .and(Episodes.quality eq TVShows.quality)
            }
            .map { it[Episodes.id] }

        Episodes.update({ Episodes.id inList downloadingEpisodeIDs }) {
            it[state] = EpisodeState.DOWNLOADING
        }

        Episodes.update({ Episodes.id inList storedEpisodesIDs }) {
            it[state] = EpisodeState.STORED
        }

        SchemaUtils.drop(Downloads)
    }
}

private fun removeDbExtension(path: String): String {
    return if (path.endsWith(DB_EXTENSION)) {
        path.dropLast(DB_EXTENSION.length)
    } else {
        path
    }
}