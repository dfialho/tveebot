package dfialho.tveebot.downloader

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path

@ConfigurationProperties("downloader")
class DownloaderConfig {
    lateinit var savePath: Path
}