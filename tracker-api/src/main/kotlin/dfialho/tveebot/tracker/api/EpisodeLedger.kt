package dfialho.tveebot.tracker.api

import dfialho.tveebot.app.api.models.EpisodeFile

interface EpisodeLedger {

    fun appendOrUpdate(episodeFile: EpisodeFile): Boolean
}
