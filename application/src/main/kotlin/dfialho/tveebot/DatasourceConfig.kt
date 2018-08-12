package dfialho.tveebot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
@ConfigurationProperties("datasource")
class DatasourceConfig {
    var savePath: Path = Paths.get("tveebot")
}
