package dfialho.tveebot.repositories.impl

import dfialho.tveebot.application.api.EpisodeState
import dfialho.tveebot.data.TrackerRepository
import dfialho.tveebot.downloader.api.DownloadEngine
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.episodeFileOf
import dfialho.tveebot.repositories.DownloadPool
import dfialho.tveebot.toPrettyString
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.tvShow
import mu.KLogging

class DownloadPoolImpl(private val repository: TrackerRepository) : DownloadPool {

    companion object : KLogging()

    private val episodesByReference = mutableMapOf<DownloadReference, EpisodeFile>()

    override fun reload(engine: DownloadEngine) {

        val episodeFiles = mutableListOf<EpisodeFile>()

        repository.findTrackedTVShows().forEach { tvShowEntity ->
            repository.findEpisodesFrom(tvShowEntity.id)
                .filter { it.state == EpisodeState.AVAILABLE || it.state == EpisodeState.DOWNLOADING }
                .mapTo(episodeFiles) { episodeFileOf(tvShowEntity, it) }
        }

        episodeFiles.forEach {
            logger.debug { "Restarting download of episode: ${it.toPrettyString()}" }
            val handle = engine.add(it.link)
            episodesByReference[handle.reference] = it
        }
    }

    override fun get(reference: DownloadReference): EpisodeFile? {
        return episodesByReference[reference]
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