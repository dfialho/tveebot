package dfialho.tveebot.app

import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration

fun appConfig(
    checkPeriod: Duration = Duration.ofSeconds(1),
    downloadDirectory: Path = Paths.get("/downloads"),
    libraryDirectory: Path = Paths.get("/library"),
    databasePath: Path = Paths.get("/tveebot.db")
) = AppConfig(checkPeriod, downloadDirectory, libraryDirectory, databasePath)
