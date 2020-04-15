package dfialho.tveebot.app.rest

import dfialho.tveebot.app.repositories.TVeebotRepository
import dfialho.tveebot.app.services.DownloaderService
import dfialho.tveebot.app.services.TrackerService
import dfialho.tveebot.downloader.api.DownloadStatus

class RestManager(
    private val tracker: TrackerService,
    private val downloader: DownloaderService,
    private val repository: TVeebotRepository
) {

    fun register(registration: Registration) {
        tracker.register(registration.id, registration.videoQuality)
    }

    fun unregister(tvShowId: String) {
        tracker.unregister(tvShowId)
    }

    fun getStatus(): Status {

        return repository.transaction {
            val episodes = findEpisodes()

            Status(episodes.groupBy { it.episode.tvShow }
                .map { (tvShow, episodes) ->
                    TVShowStatus(
                        id = tvShow.id,
                        title = tvShow.title,
                        episodes = episodes.map {
                            EpisodeStatus(
                                season = it.episode.season,
                                number = it.episode.number,
                                title = it.episode.title,
                                state = it.state
                            )
                        }
                    )
                }
            )
        }
    }

    fun getDownloadStatus(): List<DownloadStatus> {
        return downloader.getStatus().sortedByDescending { it.progress }
    }
}