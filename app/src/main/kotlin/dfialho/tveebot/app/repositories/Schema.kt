package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.VideoQuality
import org.jetbrains.exposed.sql.Table

internal object TVShows : Table() {
    val ID = varchar("ID", length = 256).primaryKey()
    val TITLE = varchar("TITLE", length = 256)
    val TRACKED = bool("TRACKED").default(false)
    val VIDEO_QUALITY = enumeration("VIDEO_QUALITY", VideoQuality::class.java).default(VideoQuality.default())
}

internal object Episodes : Table() {
    val ID = varchar("ID", length = 256).primaryKey()
    val TVSHOW_ID = reference("TVSHOW_ID", TVShows.ID)
    val SEASON = integer("SEASON")
    val NUMBER = integer("NUMBER")
    val TITLE = varchar("TITLE", length = 256)
    val STATE = enumeration("STATE", State::class.java).default(State.FOUND)
}

internal object Files : Table() {
    val LINK = varchar("LINK", length = 2048).primaryKey()
    val QUALITY = enumeration("QUALITY", VideoQuality::class.java).default(VideoQuality.default())
    val PUBLISHED_DATE = datetime("PUBLISHED_DATE")
}

internal object EpisodeFiles : Table() {
    val EPISODE_ID = reference("EPISODE_ID", Episodes.ID).primaryKey()
    val FILE_ID = reference("FILE_ID", Files.LINK).primaryKey()
}

internal object Downloads : Table() {
    val ID = varchar("ID", 512).primaryKey()
    val FILE_ID = reference("FILE_ID", Files.LINK)
}

internal object StashFiles : Table() {
    val NAME = varchar("NAME", 128).primaryKey()
    val FILE_ID = varchar("FILE_ID", length = 2048)
}
