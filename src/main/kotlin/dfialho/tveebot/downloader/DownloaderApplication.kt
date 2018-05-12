package dfialho.tveebot.downloader

import dfialho.tveebot.downloader.libtorrent.LibTorrentDownloadEngine
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
        print("Closing...  ")
        downloadManager.stop()
        println("done!")
    })

    val exitLatch = CountDownLatch(1)
    val listener: EventListener = object : EventListener {
        override fun onDownloadFinished(reference: DownloadReference) {
            println("Download is complete")
            exitLatch.countDown()
        }
    }

    downloadManager.engine.addListener(listener)

    val downloadHandle = when (option) {
        "-f" -> downloadManager.engine.add(torrentFile = Paths.get(torrentReference))
        "-l" -> downloadManager.engine.add(magnetLink = torrentReference)
        else -> { usage(); exitProcess(1) }
    }

    while (!exitLatch.await(1, TimeUnit.SECONDS)) {
        val status = downloadHandle.getStatus()

        println("${status.name} (${status.state}): %.2f%% - ${status.rate / 1000} kB/s".format(status.progress * 100))
    }

}
