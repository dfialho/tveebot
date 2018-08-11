package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackerRepository
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Repository

@Repository
class TrackerRepositoryImpl : TrackerRepository, InitializingBean {

    override fun put(tvShow: TVShow) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllTVShows(): List<TVShow> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun put(episode: Episode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllEpisodes(): List<Episode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllEpisodesFrom(tvShow: TVShow): List<Episode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterPropertiesSet() {
        println("DAVID")
    }
}