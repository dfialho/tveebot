package dfialho.tveebot.downloader

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

@Configuration
@ConfigurationProperties("downloader")
class DownloaderConfig {
    lateinit var savePath: Path
}