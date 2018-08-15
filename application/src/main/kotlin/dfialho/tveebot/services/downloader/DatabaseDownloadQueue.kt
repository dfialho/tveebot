package dfialho.tveebot.services.downloader

import dfialho.tveebot.downloader.api.DownloadLink
import dfialho.tveebot.downloader.api.DownloadReference
import dfialho.tveebot.downloader.libtorrent.MagnetLink
import dfialho.tveebot.downloader.libtorrent.TorrentFile
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Paths

/**
 * Implementation of a [DownloadQueue] which persists the downloads in a database. This component is used by the
 * downloader service to resume the downloads from a previous session when the application is restarted.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@Component
@Transactional
class DatabaseDownloadQueue(private val transactionTemplate: TransactionTemplate) : DownloadQueue, InitializingBean {

    private object Downloads : Table() {
        val reference = varchar("reference", length = 256).primaryKey()
        val link = varchar("link", length = 1024)
        val type = varchar("type", length = 32)
    }

    companion object {
        const val DOWNLOAD_TYPE_FILE = "FILE"
        const val DOWNLOAD_TYPE_LINK = "LINK"
    }

    override fun afterPropertiesSet() {
        transactionTemplate.execute {
            SchemaUtils.create(Downloads)
        }
    }

    override fun push(reference: DownloadReference, link: DownloadLink) {
        Downloads.insertIgnore {
            it[Downloads.reference] = reference.value
            it[Downloads.link] = link.raw
            it[type] = if (link is TorrentFile) DOWNLOAD_TYPE_FILE else DOWNLOAD_TYPE_LINK
        }
    }

    override fun getLinks(): List<DownloadLink> = Downloads
        .selectAll()
        .map {
            if (it[Downloads.type] == DOWNLOAD_TYPE_FILE)
                TorrentFile(Paths.get(it[Downloads.link]))
            else
                MagnetLink(it[Downloads.link])
        }

    override fun remove(reference: DownloadReference) {
        Downloads.deleteWhere { Downloads.reference eq reference.value }
    }
}