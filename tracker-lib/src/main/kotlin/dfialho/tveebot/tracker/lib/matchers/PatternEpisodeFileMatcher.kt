package dfialho.tveebot.tracker.lib.matchers

import dfialho.tveebot.app.api.models.VideoQuality
import dfialho.tveebot.app.api.models.toVideoQualityOrNull
import dfialho.tveebot.tracker.api.EpisodeFileMatcher
import dfialho.tveebot.tracker.api.MatchedEpisode
import dfialho.tveebot.tracker.api.MatchedEpisodeFile
import dfialho.tveebot.tracker.api.PatternProvider
import mu.KLogging

class PatternEpisodeFileMatcher(private val patternProvider: PatternProvider) : EpisodeFileMatcher {

    companion object : KLogging()

    override fun match(rawName: String): MatchedEpisodeFile? {

        val patterns = patternProvider.get()
        val delimiters = DefaultPatterns.delimiters.toPattern()

        for (pattern in patterns) {
            val matcher = pattern.matcher(rawName)

            if (matcher.matches()) {
                logger.debug { "Match found: name '$rawName' matched pattern '$pattern'" }

                val episodeTitle = matcher.group("title").orEmpty()
                    .split(delimiters)
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { it.capitalize() }

                val season = matcher.group("season").toInt()
                val number = matcher.group("number").toInt()

                val videoQuality = try {
                    matcher.group("quality").toVideoQualityOrNull() ?: VideoQuality.default()
                } catch (e: IllegalArgumentException) {
                    VideoQuality.default()
                }

                val extraNumber: Int = try {
                    matcher.group("extraNumber").toInt()
                } catch (e: IllegalArgumentException) {
                    number
                }

                return MatchedEpisodeFile(
                    videoQuality = videoQuality,
                    episodes = (number..extraNumber).map { MatchedEpisode(episodeTitle, season, it) }
                )
            }
        }

        logger.debug { "No match found for: $rawName" }
        return null
    }
}
