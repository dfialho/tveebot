package dfialho.tveebot.tracker.lib

import com.google.common.hash.Hashing
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.ID
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.VideoQuality
import java.nio.charset.Charset

object EpisodeIDGenerator {

    private val hashFunction = Hashing.sha256()

    fun getID(tvShow: TVShow, episode: EpisodeFile): ID {
        return getID(tvShow.id, episode.season, episode.number, episode.quality)
    }

    fun getID(tvShowEpisode: TVShowEpisodeFile): ID {
        return with(tvShowEpisode) {
            getID(tvShowID, season, number, quality)
        }
    }

    fun getID(tvShowID: ID, season: Int, number: Int, quality: VideoQuality): ID {

        val hashID = hashFunction.newHasher()
            .putString(tvShowID.value, Charset.defaultCharset())
            .putInt(season)
            .putInt(number)
            .putString(quality.toString(), Charset.defaultCharset())
            .hash()
            .toString()

        return ID(hashID)
    }
}
