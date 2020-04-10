package dfialho.tveebot.tracker.api

import java.util.regex.Pattern

interface PatternProvider {
    fun get(): List<Pattern>
}
