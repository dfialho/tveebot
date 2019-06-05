package dfialho.tveebot.repositories.impl

import dfialho.tveebot.application.api.ID
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.repositories.DownloadPool
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.tvShow
import mu.KLogging

class InMemoryDownloadPool : DownloadPool {

    companion object : KLogging()

    private val episodesByReference = mutableMapOf<DownloadReference, EpisodeFile>()

    override fun put(reference: DownloadReference, episodeFile: EpisodeFile) {
        episodesByReference[reference] = episodeFile
    }

    override fun listByTVShow(tvShowID: ID): List<Pair<DownloadReference, EpisodeFile>> {

        return episodesByReference.entries
            .filter { (_, episodeFile) -> episodeFile.tvShow.id == tvShowID }
            .map { (reference, episodeFile) -> Pair(reference, episodeFile) }
    }

    override fun remove(reference: DownloadReference): EpisodeFile? {
        return episodesByReference.remove(reference)
    }
}