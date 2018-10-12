package dfialho.tveebot.services.models

import dfialho.tveebot.downloader.api.DownloadReference
import java.nio.file.Path

data class FinishedDownloadParameters(
    val reference: DownloadReference,
    val savePath: Path
)