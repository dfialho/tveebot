package dfialho.tveebot.testing

import dfialho.tveebot.tracker.api.models.Episode
import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.tracker.api.models.VideoQuality
import dfialho.tveebot.tracker.api.models.randomID
import java.time.Instant
import java.util.*

private val random = Random(0)

fun anyTVShow() = TVShow(
    id = randomID(),
    title = "Show ${randomString()}",
    quality = VideoQuality.HD
)

fun anyEpisode(season: Int? = null, number: Int? = null) = Episode(
    title = "Episode ${randomString()}",
    season = season ?: random.nextInt(10),
    number = number ?: random.nextInt(25)
)

fun anyEpisodeFile(
    season: Int? = null,
    number: Int? = null,
    quality: VideoQuality = VideoQuality.default()
) = EpisodeFile(
    episode = anyEpisode(season, number),
    quality = quality,
    link = "magnet://${randomString()}",
    publishDate = Instant.ofEpochMilli(random.nextLong())
)

fun anyTVShowEpisodeFile(
    tvShow: TVShow = anyTVShow(),
    season: Int? = null,
    number: Int? = null,
    quality: VideoQuality = VideoQuality.default()
): TVShowEpisodeFile {

    return with(anyEpisodeFile(season, number, quality)) {
        TVShowEpisodeFile(
            episode = TVShowEpisode(
                tvShow.id,
                tvShow.title,
                this.title,
                this.season,
                this.number
            ),
            quality = this.quality,
            link = this.link,
            publishDate = this.publishDate
        )
    }
}

private fun randomString(): String = UUID.randomUUID().toString().take(5)
