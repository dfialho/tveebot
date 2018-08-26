package dfialho.tveebot.testing

import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.VideoQuality
import java.time.Instant
import java.util.*

/**
 * Returns a randomly generated [Instant].
 */
fun randomInstant(): Instant = Instant.ofEpochSecond(Math.random().toInt().toLong())

/**
 * Returns a random string.
 */
fun randomString(): String = UUID.randomUUID().toString()

/**
 * Generates a list containing [n] random TV shows and returns the list.
 */
fun randomTVShows(n: Int, tracked: Boolean = false, quality: VideoQuality = VideoQuality.default()): List<TVShow> {
    val title = randomString()
    return (1..n).map { TVShow(UUID.randomUUID(), "$title-$it", quality, tracked) }
}

/**
 * Generates a single random TV show and returns it.
 */
fun randomTVShow(tracked: Boolean = false, quality: VideoQuality = VideoQuality.default()): TVShow {
    return randomTVShows(1, tracked, quality).first()
}

/**
 * Generates a random [DownloadReference] and returns it.
 */
fun randomDownloadReference() = DownloadReference(randomString())

/**
 * Generates [n] random episodes with the specified [season] number and returns a list containing those episodes.
 * Episodes generated ara guaranteed to have different episode numbers.
 */
fun randomEpisodes(n: Int, season: Int = 1, quality: VideoQuality = VideoQuality.default()): List<EpisodeFile> {
    return (1..n).map { number -> randomEpisode(season, number, quality) }
}

/**
 * Generates a single random episode file and returns it.
 */
fun randomEpisode(
    season: Int = 1,
    number: Int = 1,
    quality: VideoQuality = VideoQuality.default()
): EpisodeFile {
    val salt = randomString()

    return EpisodeFile(
        Episode("$salt-title", season, number),
        quality,
        "$salt-link",
        randomInstant()
    )
}
