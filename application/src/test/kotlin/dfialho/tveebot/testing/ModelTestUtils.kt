package dfialho.tveebot.testing

import dfialho.tveebot.tracker.api.models.*
import java.time.Instant
import java.util.*

private val random = Random(0)

fun anyTVShow() = TVShow(
    id = randomID(),
    title = "Show ${randomString()}"
)

fun anyEpisode(tvShow: TVShow = anyTVShow(), season: Int? = null, number: Int? = null) = Episode(
    tvShow = tvShow,
    title = "Episode ${randomString()}",
    season = season ?: random.nextInt(10),
    number = number ?: random.nextInt(25)
)

fun anyEpisodeFile(
    tvShow: TVShow = anyTVShow(),
    season: Int? = null,
    number: Int? = null,
    quality: VideoQuality = VideoQuality.default()
) = EpisodeFile(
    episode = anyEpisode(tvShow, season, number),
    quality = quality,
    link = "magnet://${randomString()}",
    publishDate = Instant.ofEpochMilli(random.nextLong())
)

private fun randomString(): String = UUID.randomUUID().toString().take(5)
