package dfialho.tveebot.utils.rssfeed

import java.time.Instant

/**
 * Data class containing the data included in an item from an [RSSFeed].
 * It does not contain everything. Only a subset of the information.
 *
 * @property title The title of the item
 * @property link The link to obtain the content of the item
 * @property publishedDate The date at which the item was published
 */
data class RSSFeedItem internal constructor(
    val title: String,
    val link: String,
    val publishedDate: Instant
)
