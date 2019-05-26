package dfialho.tveebot.repositories

import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.ID

interface DownloadPool {

    fun reload(engine: DownloadEngine)

    fun get(reference: DownloadReference): EpisodeFile?

    fun put(reference: DownloadReference, episodeFile: EpisodeFile)

    fun listByTVShow(tvShowID: ID): List<Pair<DownloadReference, EpisodeFile>>

    fun remove(reference: DownloadReference): EpisodeFile?
}