package dfialho.tveebot.repositories.impl

import dfialho.tveebot.application.api.EpisodeState
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.episodeFileOf
import dfialho.tveebot.repositories.DownloadPool
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.tvShow
import mu.KLogging

class PersistentDownloadPool(private val repository: TrackerRepository) : DownloadPool {

    companion object : KLogging()

    private val episodesByReference = mutableMapOf<DownloadReference, EpisodeFile>()

    override fun listUnstarted(): List<EpisodeFile> {
        val episodeFiles = mutableListOf<EpisodeFile>()

        repository.findTrackedTVShows().forEach { tvShowEntity ->
            repository.findEpisodesFrom(tvShowEntity.id).asSequence()
                .filter { it.state == EpisodeState.AVAILABLE || it.state == EpisodeState.DOWNLOADING }
                .map { episodeFileOf(tvShowEntity, it) }
                .filter { !episodesByReference.values.contains(it) }
                .filter { it.quality == tvShowEntity.quality }
                .mapTo(episodeFiles) { it }
        }

        return episodeFiles
    }

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