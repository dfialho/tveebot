package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun usage() {
    println("Usage: ")
    println("  downloader <save-directory> -f <torrent-file>")
    println("  downloader <save-directory> -l <magnet-link>")
}

fun main(args: Array<String>) {

    if (args.size != 3) {
        usage()
        exitProcess(1)
    }

    val savePath = Paths.get(args[0])
    val option: String = args[1]
    val torrentReference: String = args[2]

    if (!Files.isDirectory(savePath)) {
        println("The download directory does not exists: $savePath")
        exitProcess(1)
    }

    val downloadManager = DownloadManager(LibTorrentDownloadEngine(savePath))
    downloadManager.start()

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        println()
        println("Closing...")
        downloadManager.stopAsync()
        downloadManager.awaitTerminated()
    })

    val listener: EventListener = object : EventListener {
        override fun onDownloadFinished(reference: DownloadReference) {
            downloadManager.stopAsync()
        }
    }

    downloadManager.engine.addListener(listener)

    when (option) {
        "-f" -> downloadManager.engine.add(torrentFile = Paths.get(torrentReference))
        "-l" -> downloadManager.engine.add(magnetLink = torrentReference)
        else -> { usage(); exitProcess(1) }
    }

    print("Downloading...  ")
    downloadManager.awaitTerminated()
    println("done!")

}
