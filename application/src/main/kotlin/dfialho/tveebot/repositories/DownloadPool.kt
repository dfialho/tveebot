package dfialho.tveebot.repositories

import dfialho.tveebot.application.api.ID
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.models.EpisodeFile

interface DownloadPool {

    fun put(reference: DownloadReference, episodeFile: EpisodeFile)

    fun listByTVShow(tvShowID: ID): List<Pair<DownloadReference, EpisodeFile>>

    fun remove(reference: DownloadReference): EpisodeFile?
}