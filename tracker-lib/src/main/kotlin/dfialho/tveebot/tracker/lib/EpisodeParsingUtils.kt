package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.Episode
import dfialho.tveebot.tracker.api.VideoQuality
import dfialho.tveebot.tracker.api.videoQualityFromIdentifier
import java.util.regex.Pattern

/**
 * Pattern for the episode number. The pattern should include the season an number separated by an 'x'. For instance,
 * "12x23", where the season is 12 and number is 23.
 */
private val episodeNumberPattern by lazy { Pattern.compile("\\d+x\\d+") }

/**
 * Set of tokens to ignore in an episode title.
 */
private val ignoredTokens: Collection<String> = setOf(
    "PROPER",
    "TBA",
    "REPACK"
)

/**
 * Parses the episode full [title] and returns the episode and the video quality.
 *
 * @throws IllegalArgumentException If the format of [title] is invalid
 */
internal fun parseEpisodeFilename(title: String): Pair<Episode, VideoQuality> {
    require(title.isNotBlank()) { "episode full title cannot be blank" }

    val tokensWithQuality = title
        .split(' ')
        .filter { it.isNotBlank() }
        .filter { it !in ignoredTokens }
        .map { it.trim() }

    var quality: VideoQuality? = videoQualityFromIdentifier(tokensWithQuality.last())

    val tokens: List<String>
    if (quality == null) {
        quality = VideoQuality.SD
        tokens = tokensWithQuality
    } else {
        tokens = tokensWithQuality.dropLast(1)
    }

    // Find the index where the episode number pattern is
    val episodeNumberTokenIndex = tokens.indexOfFirst { episodeNumberPattern.matcher(it).matches() }

    if (episodeNumberTokenIndex == -1) {
        throw IllegalArgumentException("episode full title must include the token '${episodeNumberPattern.pattern()}'")
    }

    // Parse the episode number token - should be something like "12x23"
    val seasonAndNumber: List<String> = tokens[episodeNumberTokenIndex].split('x')

    val episode = Episode(
        season = seasonAndNumber.first().toShort(),
        number = seasonAndNumber.last().toShort(),

        // The title of the episode corresponds to everything after the episode number token
        title = tokens.subList(episodeNumberTokenIndex + 1, tokens.lastIndex + 1).joinToString(" ")
    )

    return Pair(episode, quality)
}
