package dfialho.tveebot.tracker.lib.matchers

import org.intellij.lang.annotations.Language

object DefaultPatterns {

    const val delimiters = "[ ]"
    const val qualityTags = "(480p|720p|1080p)"
    const val ignoredTags = "(REPACK|PROPER)"

    @Suppress("RegExpRepeatedSpace", "RegExpUnexpectedAnchor")
    @Language("RegExp")
    val patterns = listOf(
        // Show Name 1x02-03 Episode Title 720p
        """
            ^(?<tvShowTitle>.+?)${delimiters}+
            (?<season>\d+)x(?<number>\d+)-(?<extraNumber>\d+)
            (?<title>.*?)${delimiters}+
            (?<quality>${qualityTags})
            (${delimiters}+${ignoredTags})*$
        """.singleLine(),
        // Show Name 1x02-03 Episode Title
        """
            ^(?<tvShowTitle>.+?)${delimiters}+
            (?<season>\d+)x(?<number>\d+)-(?<extraNumber>\d+)
            (${ignoredTags}|(?<title>.*?))
            (${delimiters}+${ignoredTags})*$
        """.singleLine(),
        // Show Name 1x02 Episode Title 720p
        """
            ^(?<tvShowTitle>.+?)${delimiters}+
            (?<season>\d+)x(?<number>\d+)${delimiters}*
            (?<title>.*?)${delimiters}+
            (?<quality>${qualityTags})
            (${delimiters}+${ignoredTags})*$
        """.singleLine(),
        // Show Name 1x02 Episode Title
        """
            ^(?<tvShowTitle>.+?)${delimiters}+
            (?<season>\d+)x(?<number>\d+)${delimiters}*
            (${ignoredTags}|(?<title>.*?))
            (${delimiters}+${ignoredTags})*$
        """.singleLine()
    )

    private fun String.singleLine(): String {
        return this.lines()
            .joinToString(separator = "") { it.trim() }
    }
}
