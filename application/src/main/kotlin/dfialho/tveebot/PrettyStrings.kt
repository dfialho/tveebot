package dfialho.tveebot

import dfialho.tveebot.tracker.api.models.EpisodeFile
import dfialho.tveebot.tracker.api.models.TVShow

fun EpisodeFile.toPrettyString(): String {
    return "${episode.tvShow.title} - ${episode.season}x%02d - ${episode.title} ($quality)".format(episode.number)
}

fun TVShow.toPrettyString(): String {
    return "[$id] $title"
}
