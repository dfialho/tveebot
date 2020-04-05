package dfialho.tveebot.tracker.api

interface VideoFileParser {
    fun parse(description: String): ParsedEpisodeFile
}
