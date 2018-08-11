package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.EpisodeFile
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.VideoQuality
import dfialho.tveebot.utils.rssfeed.RSSFeedItem
import dfialho.tveebot.utils.rssfeed.RSSFeedReader
import java.net.URL

/**
 * [TVShowProvider] backed by the "showrss.info" website.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ShowRSSProvider(private val idMapper: TVShowIDMapper) : TVShowProvider {

    /**
     * Reader used to read the feed obtained from showRSS and find the episodes available.
     */
    private val feedReader = RSSFeedReader()

    override fun fetchEpisodes(tvShow: TVShow): List<EpisodeFile> {
        val showID: String = idMapper[tvShow.id] ?: throw IllegalArgumentException("Not found: $tvShow")
        val showURL = URL("https://showrss.info/show/$showID.rss")

        val rssFeed = feedReader.read(showURL)
        return rssFeed.items
            .map { it.toEpisodeVideo() }
            .distinctByMostRecent()
    }
}

/**
 * Converts this [RSSFeedItem] into an [EpisodeFile] and returns the result.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
internal fun RSSFeedItem.toEpisodeVideo(): EpisodeFile {
    val (episode, quality) = parseEpisodeFilename(this.title)

    return EpisodeFile(
        episode = episode,
        quality = quality,
        link = this.link,
        publishedDate = this.publishedDate
    )
}

private fun Iterable<EpisodeFile>.distinctByMostRecent(): List<EpisodeFile> {
    val distinctEpisodes = mutableMapOf<EpisodeIdentifier, EpisodeFile>()

    for (episodeFile in this) {
        distinctEpisodes.merge(episodeFile.identifier, episodeFile) { oldFile, newFile ->
            // Update existing episode only if the new one is more recent
            if (newFile.publishedDate.isAfter(oldFile.publishedDate)) { newFile } else { oldFile }
        }
    }

    return distinctEpisodes.values.toList()
}

