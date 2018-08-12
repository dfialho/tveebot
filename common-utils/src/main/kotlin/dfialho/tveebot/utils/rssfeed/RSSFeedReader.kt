package dfialho.tveebot.utils.rssfeed

import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
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
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class RSSFeedReader {

    /**
     * Internal feed parser.
     */
    private val syndFeedInput = SyndFeedInput()

    /**
     * Reads an RSS feed from a [file] and returns the result in a [RSSFeed].
     */
    fun read(file: File): RSSFeed {
        return syndFeedInput.build(XmlReader(file)).toRSSFeed()
    }

    /**
     * Reads an RSS feed from an [URL] and returns the result in a [RSSFeed].
     */
    fun read(url: URL): RSSFeed {
        return syndFeedInput.build(XmlReader(url)).toRSSFeed()
    }

    /**
     * Reads an RSS feed from an [InputStream] and returns the result in a [RSSFeed].
     */
    fun read(inputStream: InputStream): RSSFeed {
        return syndFeedInput.build(XmlReader(inputStream)).toRSSFeed()
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
        items = syndEntries.map { RSSFeedItem(
            it.title.orEmpty(),
            it.link.orEmpty(),
            it.publishedDate.toInstant())
        }
    )
}
