package dfialho.tveebot.app

import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration

fun appConfig(
    checkPeriod: Duration = Duration.ofSeconds(1),
    downloadingDirectory: Path = Paths.get("/downloading"),
    downloadedDirectory: Path = Paths.get("/downloaded"),
    libraryDirectory: Path = Paths.get("/library"),
    databasePath: Path = Paths.get("/tveebot.db")
) = AppConfig(checkPeriod, downloadingDirectory, downloadedDirectory, libraryDirectory, databasePath)
