package dfialho.tveebot.services

import dfialho.tveebot.services.models.FinishedDownloadParameters
import dfialho.tveebot.services.models.NewEpisodeParameters
import dfialho.tveebot.tracker.api.models.TVShow

object Alerts {
    val NewEpisodeFound = Alert<NewEpisodeParameters>()
    val DownloadFinished = Alert<FinishedDownloadParameters>()
    val StartedTrackingTVShow = Alert<TVShow>()
    val StoppedTrackingTVShow = Alert<TVShow>()
}
