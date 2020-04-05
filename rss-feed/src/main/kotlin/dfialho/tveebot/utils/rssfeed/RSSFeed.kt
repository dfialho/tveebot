package dfialho.tveebot.utils.rssfeed

/**
 * Data class containing the information about an RSS feed.
 *
 * @property title The title of the feed
 * @property items The items included in the feed
 */
data class RSSFeed(
    val title: String,
    val items: List<RSSFeedItem> = emptyList()
)
