package dfialho.tveebot.app.components

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.downloader.api.DownloadReference

interface DownloadTracker {
    operator fun set(reference: DownloadReference, episodeFile: EpisodeFile)
    operator fun get(reference: DownloadReference): EpisodeFile?
    fun list(): List<EpisodeFile>
    fun remove(reference: DownloadReference)
}
