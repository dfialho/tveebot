package dfialho.tveebot.tracker.lib

import dfialho.tveebot.app.api.models.Episode
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShow
import dfialho.tveebot.app.api.models.VideoFile
import dfialho.tveebot.tracker.api.EpisodeFileMatcher
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.utils.rssfeed.RSSFeedException
import dfialho.tveebot.utils.rssfeed.RSSFeedItem
import dfialho.tveebot.utils.rssfeed.RSSFeedReader
import mu.KLogging
import java.net.URL

/**
 * [TVShowProvider] backed by the "showrss.info" website.
 */
class ShowRSSProvider(private val matcher: EpisodeFileMatcher) : TVShowProvider {

    companion object : KLogging()

    /**
     * Reader used to read the feed obtained from showRSS and find the episodes available.
     */
    private val feedReader = RSSFeedReader()

    override fun fetchTVShow(tvShowId: String): TVShow? {

        val title = feedReader.read(tvShowURL(tvShowId))
            .title
            .replaceFirst("showRSS feed: ", "")

        return TVShow(tvShowId, title)
    }

    override fun fetchEpisodes(tvShow: TVShow): List<EpisodeFile> {

        return feedReader.read(tvShowURL(tvShow.id))
            .items
            .mapNotNull { parseEpisodeOrNull(it, tvShow) }
    }

    private fun tvShowURL(tvShowId: String) = URL("https://showrss.info/show/${tvShowId}.rss")

    private fun parseEpisodeOrNull(item: RSSFeedItem, tvShow: TVShow): EpisodeFile? {

        return try {
            parseEpisode(item, tvShow)
        } catch (e: RSSFeedException) {
            logger.warn { "Failed to obtain episode information from RSS item: $this" }
            null
        }
    }

    /**
     * Converts this [RSSFeedItem] into an [EpisodeFile] and returns the result.
     *
     * @throws RSSFeedException if it fails to find the episode information from the feed
     * @author David Fialho (dfialho@protonmail.com)
     */
    private fun parseEpisode(item: RSSFeedItem, tvShow: TVShow): EpisodeFile? {

        val matchedEpisodeFile = try {
            matcher.match(item.title)
        } catch (e: IllegalArgumentException) {
            throw RSSFeedException("Failed to parse episode information from RSS item: $this", e)
        }

        return matchedEpisodeFile?.let { matched ->
            EpisodeFile(
                VideoFile(item.link, matched.videoQuality, item.publishedDate),
                episodes = matched.episodes
                    .map { Episode(tvShow, it.season, it.number, it.title) }
            )
        }
    }
}
