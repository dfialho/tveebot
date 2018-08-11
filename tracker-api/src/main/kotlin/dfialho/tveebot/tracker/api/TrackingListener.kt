package dfialho.tveebot.tracker.api

interface TrackingListener {
    fun notify(tvShow: TVShow, episodeFile: EpisodeFile)
}