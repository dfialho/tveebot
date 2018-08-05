package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.*
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

    override fun fetchEpisodes(tvShow: TVShow): List<EpisodeVideo> {
        val showID: String = idMapper[tvShow.id] ?: throw IllegalArgumentException("Not found: $tvShow")
        val showURL = URL("https://showrss.info/show/$showID.rss")

        val rssFeed = feedReader.read(showURL)
        return rssFeed.items.map { it.toEpisodeVideo() }
    }
}

/**
 * Converts this [RSSFeedItem] into an [EpisodeVideo] and returns the result.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
internal fun RSSFeedItem.toEpisodeVideo(): EpisodeVideo {
    val (episode, quality) = parseEpisodeFilename(this.title)

    return EpisodeVideo(
        episode = episode,
        quality = quality,
        link = this.link,
        publishedDate = this.publishedDate
    )
}



