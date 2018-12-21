package dfialho.tveebot.tracker.lib

import dfialho.tveebot.tracker.api.TVShowIDMapper
import dfialho.tveebot.tracker.api.TVShowProvider
import dfialho.tveebot.tracker.api.models.EpisodeID
import dfialho.tveebot.tracker.api.models.TVShow
import dfialho.tveebot.tracker.api.models.TVShowEpisode
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile
import dfialho.tveebot.utils.rssfeed.RSSFeedException
import dfialho.tveebot.utils.rssfeed.RSSFeedItem
import dfialho.tveebot.utils.rssfeed.RSSFeedReader
import org.jsoup.Jsoup
import java.net.URL

/**
 * [TVShowProvider] backed by the "showrss.info" website.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class ShowRSSProvider(private val idMapper: TVShowIDMapper) : TVShowProvider {

    companion object {
        private const val SHOWRSS_URL = "https://showrss.info/browse"
    }

    /**
     * Reader used to read the feed obtained from showRSS and find the episodes available.
     */
    private val feedReader = RSSFeedReader()

    override fun fetchTVShows(): List<TVShow> = Jsoup.connect(SHOWRSS_URL).get()
        .select("option")
        .map {
            TVShow(
                id = idMapper.getTVShowID(providerID = it.attr("value")),
                title = it.text()
            )
        }

    override fun fetchEpisodes(tvShow: TVShow): List<TVShowEpisodeFile> {
        val showID: String = idMapper[tvShow.id] ?: throw IllegalArgumentException("Not found: $tvShow")
        val showURL = URL("https://showrss.info/show/$showID.rss")

        val rssFeed = feedReader.read(showURL)
        return rssFeed.items
            .map { it.parseEpisode(tvShow) }
            .distinctByMostRecent()
    }
}

/**
 * Converts this [RSSFeedItem] into an [TVShowEpisodeFile] and returns the result.
 *
 * @throws RSSFeedException if it fails to find the episode information from the feed
 * @author David Fialho (dfialho@protonmail.com)
 */
internal fun RSSFeedItem.parseEpisode(tvShow: TVShow): TVShowEpisodeFile {
    val (episode, quality) = try {
        parseEpisodeTitle(this.title)
    } catch (e: IllegalArgumentException) {
        throw RSSFeedException("Failed to parse episode information from ${this.title}", e)
    }

    return TVShowEpisodeFile(
        TVShowEpisode(
            tvShowID = tvShow.id,
            tvShowTitle = tvShow.title,
            title = episode.title,
            season = episode.season,
            number = episode.number
        ),
        quality = quality,
        link = this.link,
        publishDate = this.publishedDate
    )
}

/**
 * Returns a list containing only episode files from the given iterable having distinct episodes files based on
 * its ID, selecting the most recent episode file.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
private fun Iterable<TVShowEpisodeFile>.distinctByMostRecent(): List<TVShowEpisodeFile> {
    val distinctEpisodes = mutableMapOf<EpisodeID, TVShowEpisodeFile>()

    for (episodeFile in this) {

        distinctEpisodes.merge(EpisodeIDGenerator.getID(episodeFile), episodeFile) { oldFile, newFile ->
            // Update existing episode only if the new one is more recent
            if (newFile.publishDate.isAfter(oldFile.publishDate)) {
                newFile
            } else {
                oldFile
            }
        }
    }

    return distinctEpisodes.values.toList()
}

