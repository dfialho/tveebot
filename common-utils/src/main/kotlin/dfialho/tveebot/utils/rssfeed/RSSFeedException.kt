package dfialho.tveebot.utils.rssfeed

import java.io.IOException

/**
 * Thrown by the [RSSFeedReader] if it cannot parse an RSS feed.
 *
 * @param message the message describing the cause for the exception
 * @param cause the exception which caused this
 * @author David Fialho (dfialho@protonmail.com)
 */
class RSSFeedException(message: String, cause: Throwable?) : IOException(message, cause) {
    constructor(message: String): this(message, null)
}