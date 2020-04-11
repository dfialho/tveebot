package dfialho.tveebot.app

import java.nio.file.Path
import java.time.Duration

data class AppConfig(
    val checkPeriod: Duration,
    val downloadsDirectory: Path,
    val libraryDirectory: Path,
    val databasePath: Path
)
