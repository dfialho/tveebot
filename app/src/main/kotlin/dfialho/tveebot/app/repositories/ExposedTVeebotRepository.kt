package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.api.models.Episode
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.api.models.TVShow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

class ExposedTVeebotRepository(db: Database) : TVeebotRepository {

    private object TVShows : Table() {
        val ID = varchar("ID", length = 36).primaryKey()
        val TITLE = varchar("TITLE", length = 256)
        val TRACKED = bool("TRACKED")
        val VIDEO_QUALITY = varchar("VIDEO_QUALITY", length = 32)
    }

    private object Episodes : Table() {
        val ID = varchar("ID", length = 128).primaryKey()
        val TVSHOW_ID = reference("TVSHOW_ID", TVShows.ID)
        val SEASON = integer("SEASON")
        val NUMBER = integer("NUMBER")
        val TITLE = varchar("TITLE", length = 256)
    }

    override fun store(tvShow: TVShow, tracked: Boolean): TVShow {
        TODO("Not yet implemented")
    }

    override fun store(episode: Episode): Episode {
        TODO("Not yet implemented")
    }

    override fun findEpisodes(state: State?): List<Episode> {
        TODO("Not yet implemented")
    }

    override fun updateState(id: String, downloaded: State) {
        TODO("Not yet implemented")
    }
}