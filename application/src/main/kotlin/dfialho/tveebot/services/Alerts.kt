package dfialho.tveebot.services

import dfialho.tveebot.services.models.FinishedDownloadNotification
import dfialho.tveebot.services.models.NewEpisodeNotification
import dfialho.tveebot.tracker.api.models.TVShow

object Alerts {
    val NewEpisodeFound = Alert<NewEpisodeNotification>("NewEpisodeFound")
    val DownloadFinished = Alert<FinishedDownloadNotification>("DownloadFinished")
    val StartedTrackingTVShow = Alert<TVShow>("StartedTrackingTVShow")
    val StoppedTrackingTVShow = Alert<TVShow>("StoppedTrackingTVShow")
}
