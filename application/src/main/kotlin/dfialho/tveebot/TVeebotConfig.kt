package dfialho.tveebot

import java.nio.file.Path

data class TVeebotConfig(
    val checkPeriod: Long,
    val downloadingDirectory: Path,
    val libraryDirectory: Path,
    val databasePath: Path
)
