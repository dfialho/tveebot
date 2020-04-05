package dfialho.tveebot.utils.rssfeed

import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.io.FeedException
import com.sun.syndication.io.SyndFeedInput
import com.sun.syndication.io.XmlReader
import java.io.File
import java.io.InputStream
import java.net.URL

/**
 *
 * The [RSSFeedReader] is a wrapper around RSS parser implementation. It provides an abstraction for the RSS feed
 * parsing library. It is able to read RSS feeds from an XML document. The document can be passed in as a [File],
 * [URL], or [InputStream].
 */
class RSSFeedReader {

    /**
     * Internal feed parser.
     */
    private val syndFeedInput = SyndFeedInput()

    /**
     * Reads an RSS feed from a [file] and returns the result in a [RSSFeed].
     *
     * @throws IllegalArgumentException if feed type could not be understood by any of the underlying parsers.
     * @throws RSSFeedException if the feed could not be parsed
     */
    fun read(file: File): RSSFeed = read(XmlReader(file))

    /**
     * Reads an RSS feed from an [URL] and returns the result in a [RSSFeed].
     *
     * @throws IllegalArgumentException if feed type could not be understood by any of the underlying parsers.
     * @throws RSSFeedException if the feed could not be parsed
     */
    fun read(url: URL): RSSFeed = read(XmlReader(url))

    /**
     * Reads an RSS feed from an [InputStream] and returns the result in a [RSSFeed].
     *
     * @throws IllegalArgumentException if feed type could not be understood by any of the underlying parsers.
     * @throws RSSFeedException if the feed could not be parsed
     */
    fun read(inputStream: InputStream): RSSFeed = read(XmlReader(inputStream))

    /**
     * Base method to read the RSS feed from an [XmlReader].
     *
     * @throws IllegalArgumentException if feed type could not be understood by any of the underlying parsers.
     * @throws RSSFeedException if the feed could not be parsed
     */
    private fun read(reader: XmlReader): RSSFeed {
        return try {
            syndFeedInput.build(reader).toRSSFeed()
        } catch (e: FeedException) {
            throw RSSFeedException(e.message.orEmpty())
        }
    }
}

/**
 * Converts a [SyndFeed] into an [RSSFeed] instance and returns the result.
 */
private fun SyndFeed.toRSSFeed(): RSSFeed {
    @Suppress("UNCHECKED_CAST")
    val syndEntries = this.entries as List<SyndEntry>

    return RSSFeed(
        title = this.title.orEmpty(),
        items = syndEntries.map {
            RSSFeedItem(
                it.title.orEmpty(),
                it.link.orEmpty(),
                it.publishedDate.toInstant())
        }
    )
}
