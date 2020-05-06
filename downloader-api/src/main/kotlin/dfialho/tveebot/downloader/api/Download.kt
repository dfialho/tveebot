package dfialho.tveebot.downloader.api

import java.nio.file.Path

data class Download(
    val reference: DownloadReference,
    val name: String,
    val savePath: Path,
    val status: DownloadStatus
)
