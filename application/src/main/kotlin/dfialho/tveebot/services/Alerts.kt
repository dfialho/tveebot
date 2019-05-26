package dfialho.tveebot.services

import dfialho.tveebot.services.models.DownloadNotification
import dfialho.tveebot.services.models.NewEpisodeNotification
import dfialho.tveebot.services.models.StoreNotification
import dfialho.tveebot.tracker.api.models.TVShow

object Alerts {
    val NewEpisodeFound = Alert<NewEpisodeNotification>("NewEpisodeFound")
    val DownloadStarted = Alert<DownloadNotification>("DownloadStarted")
    val DownloadFinished = Alert<DownloadNotification>("DownloadFinished")
    val DownloadStopped = Alert<DownloadNotification>("DownloadStopped")
    val StartedTrackingTVShow = Alert<TVShow>("StartedTrackingTVShow")
    val StoppedTrackingTVShow = Alert<TVShow>("StoppedTrackingTVShow")
    val EpisodeStored = Alert<StoreNotification>("EpisodeStored")
}
