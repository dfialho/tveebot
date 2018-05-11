package dfialho.tveebot.downloader

/**
 * A listener for events issued by [DownloadEngine]s.
 *
 * The listener provides an handle method for each type of event. By default, all event are just ignored. That way
 * subclasses only need to implement those methods the events of which they want to handle.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface EventListener {

    /**
     * Triggered when the [DownloadEngine] determines the download is complete.
     */
    fun onDownloadFinished(reference: DownloadReference)
}