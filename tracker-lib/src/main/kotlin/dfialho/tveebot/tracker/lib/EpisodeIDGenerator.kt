package dfialho.tveebot.tracker.lib

import com.google.common.hash.Hashing
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.EpisodeID
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.TVShowID
import dfialho.tveebot.tracker.api.models.VideoQuality
import java.nio.charset.Charset

object EpisodeIDGenerator {

    private val hashFunction = Hashing.sha256()

    fun getID(tvShow: TVShow, episode: EpisodeFile): EpisodeID {
        return getID(tvShow.id, episode.season, episode.number, episode.quality)
    }

    fun getID(tvShowEpisode: TVShowEpisodeFile): EpisodeID {
        return with(tvShowEpisode) {
            getID(tvShowID, season, number, quality)
        }
    }

    fun getID(tvShowID: TVShowID, season: Int, number: Int, quality: VideoQuality): EpisodeID {

        val hashID = hashFunction.newHasher()
            .putString(tvShowID.value, Charset.defaultCharset())
            .putInt(season)
            .putInt(number)
            .putString(quality.toString(), Charset.defaultCharset())
            .hash()
            .toString()

        return EpisodeID(hashID)
    }
}
