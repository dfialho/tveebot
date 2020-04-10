package dfialho.tveebot.tracker.lib.matchers

import dfialho.tveebot.tracker.api.PatternProvider
import java.util.regex.Pattern

class LazyPatternProvider(patterns: List<String>) : PatternProvider {

    private val compiledPatterns by lazy { patterns.map { Pattern.compile(it) } }

    override fun get(): List<Pattern> {
        return compiledPatterns
    }
}
