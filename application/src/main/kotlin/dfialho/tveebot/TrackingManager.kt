package dfialho.tveebot

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackerEngine
import dfialho.tveebot.tracker.api.TrackingListener
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class TrackingManager(
    private val trackerEngine: TrackerEngine,
    private val downloadEngine: DownloadEngine
) : TrackingListener, InitializingBean, DisposableBean {

    override fun afterPropertiesSet() {
        trackerEngine.addListener(this)
    }

    override fun destroy() {
        trackerEngine.removeListener(this)
    }

    override fun notify(tvShow: TVShow, episodeFile: EpisodeFile) {
        downloadEngine.add(episodeFile.link)
    }
}