package dfialho.tveebot.tracker.api

interface EpisodeFileMatcher {
    fun match(rawName: String): MatchedEpisodeFile?
}
