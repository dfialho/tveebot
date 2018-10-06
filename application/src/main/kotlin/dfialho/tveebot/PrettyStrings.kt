package dfialho.tveebot

import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile

/**
 * Returns a pretty string representation of the episode.
 */
fun TVShowEpisodeFile.toPrettyString(): String {
    return "$tvShowTitle - ${season}x%02d - $title ($quality)".format(number)
}
