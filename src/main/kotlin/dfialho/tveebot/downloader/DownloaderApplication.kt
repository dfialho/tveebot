package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    if (args.size != 2) {
        println("Usage: downloader <save-directory> <torrent-file>")
        exitProcess(1)
    }

    val savePath: Path = Paths.get(args[0])
    val torrentFile: Path = Paths.get(args[1])

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
    downloadManager.engine.add(torrentFile)

    print("Downloading...  ")
    downloadManager.awaitTerminated()
    println("done!")

}
