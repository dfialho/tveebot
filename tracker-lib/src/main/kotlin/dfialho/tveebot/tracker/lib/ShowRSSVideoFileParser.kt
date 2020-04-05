package dfialho.tveebot.tracker.lib

import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.api.models.toVideoQualityOrNull
import dfialho.tveebot.tracker.api.ParsedEpisode
import dfialho.tveebot.tracker.api.ParsedEpisodeFile
import dfialho.tveebot.tracker.api.VideoFileParser
import java.util.regex.Pattern

class ShowRSSVideoFileParser : VideoFileParser {

    companion object {
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
    }

    // TODO Support for multiple episodes
    override fun parse(description: String): ParsedEpisodeFile {
        require(description.isNotBlank()) { "Episode description cannot be blank" }

        val tokensWithQuality = description
            .split(' ')
            .filter { it.isNotBlank() }
            .filter { it !in ignoredTokens }
            .map { it.trim() }

        var quality: VideoQuality? = tokensWithQuality.last().toVideoQualityOrNull()

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

        return ParsedEpisodeFile(
            quality = quality,
            episodes = listOf(
                ParsedEpisode(
                    title = tokens.subList(episodeNumberTokenIndex + 1, tokens.lastIndex + 1).joinToString(" "),
                    season = seasonAndNumber.first().toInt(),
                    number = seasonAndNumber.last().toInt()
                )
            )
        )
    }
}
