package dfialho.tveebot.tracker.lib

import dfialho.tveebot.app.api.models.Episode
import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.app.api.models.TVShow
import dfialho.tveebot.app.api.models.VideoFile
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.VideoFileParser
import dfialho.tveebot.utils.rssfeed.RSSFeedException
import dfialho.tveebot.utils.rssfeed.RSSFeedItem
import dfialho.tveebot.utils.rssfeed.RSSFeedReader
import mu.KLogging
import org.jsoup.Jsoup
import java.net.URL

/**
 * [TVShowProvider] backed by the "showrss.info" website.
 */
class ShowRSSProvider(private val videoFileParser: VideoFileParser) : TVShowProvider {

    companion object : KLogging() {
        private const val SHOWRSS_URL = "https://showrss.info/browse"
    }

    /**
     * Reader used to read the feed obtained from showRSS and find the episodes available.
     */
    private val feedReader = RSSFeedReader()

    override fun fetchTVShow(tvShowId: String): TVShow? {

        return Jsoup.connect("$SHOWRSS_URL/$tvShowId").get()
            .select("option")
            .map {
                TVShow(
                    id = it.attr("value"),
                    title = it.text()
                )
            }
            .firstOrNull()
    }

    override fun fetchEpisodes(tvShow: TVShow): List<EpisodeFile> {
        val showURL = URL("https://showrss.info/show/${tvShow.id}.rss")

        val rssFeed = feedReader.read(showURL)
        return rssFeed.items.mapNotNull { parseEpisodeOrNull(it, tvShow) }
    }

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
    private fun parseEpisode(item: RSSFeedItem, tvShow: TVShow): EpisodeFile {

        val parsedEpisodeFile = try {
            videoFileParser.parse(item.title)
        } catch (e: IllegalArgumentException) {
            throw RSSFeedException("Failed to parse episode information from RSS item: $this", e)
        }

        return EpisodeFile(
            VideoFile(item.link, parsedEpisodeFile.videoQuality, item.publishedDate),
            episodes = parsedEpisodeFile.episodes
                .map { Episode(tvShow, it.season, it.number, it.title) }
        )
    }
}
