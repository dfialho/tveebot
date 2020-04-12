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

fun TVShow.anyEpisode() = Episode(this, randomInt(), randomInt(), episodeTitles.random())

fun anyVideoFile(
    quality: VideoQuality = VideoQuality.default(),
    link: String = UUID.randomUUID().toString(),
    publishedDate: Instant = Instant.now()
) = VideoFile(link, quality, publishedDate)

fun TVShow.anyEpisodeFile(
    vararg episodes: Episode = arrayOf(anyEpisode()),
    file: VideoFile = anyVideoFile()
) = EpisodeFile(file, episodes.toList())

fun anyEpisodeFile(
    tvShow: TVShow = anyTVShow(),
    file: VideoFile = anyVideoFile()
) = EpisodeFile(file, listOf(anyEpisode(tvShow = tvShow)))

private fun randomInt() = Random().nextInt()
