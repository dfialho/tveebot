package dfialho.tveebot.tracker.lib

import com.google.common.hash.Hashing
import dfialho.tveebot.application.api.ID
import dfialho.tveebot.application.api.VideoQuality
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.tvShow
import java.nio.charset.Charset

object EpisodeIDGenerator {

    private val hashFunction = Hashing.sha256()

    fun getID(episodeFile: EpisodeFile): ID {
        return with(episodeFile) {
            getID(tvShow.id, episode.season, episode.number, quality)
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
