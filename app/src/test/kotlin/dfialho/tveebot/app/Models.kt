package dfialho.tveebot.app

import dfialho.tveebot.app.api.models.*
import java.time.Instant
import java.util.*

private val tvShowTitles = listOf(
    "Castle",
    "Prison Break",
    "Money Heist",
    "Game of Thrones",
    "Sherlock",
    "Friends"
)

private val episodeTitles = listOf(
    "The Pilot",
    "The One with the Sonogram at the End",
    "The One with the Thumb",
    "The One with George Stephanopoulos",
    "The One with the East German Laundry Detergent",
    "The One with the Butt",
    "The One with the Blackout",
    "The One Where Nana Dies Twice"
)

fun anyTVShow(
    id: String = randomId(),
    title: String = tvShowTitles.random()
) = TVShow(id, title)

fun anyEpisode(
    tvShow: TVShow = anyTVShow(),
    season: Int = randomInt(),
    number: Int = randomInt(),
    title: String = episodeTitles.random()
) = Episode(tvShow, season, number, title)

fun anyVideoFile(
    link: String = UUID.randomUUID().toString(),
    quality: VideoQuality = VideoQuality.default(),
    publishedDate: Instant = Instant.now()
) = VideoFile(link, quality, publishedDate)

fun anyEpisodeFile(
    tvShow: TVShow = anyTVShow(),
    file: VideoFile = anyVideoFile(),
    episode: Episode = anyEpisode(tvShow = tvShow)
) = EpisodeFile(file, listOf(episode))

private fun randomInt() = Random().nextInt()
