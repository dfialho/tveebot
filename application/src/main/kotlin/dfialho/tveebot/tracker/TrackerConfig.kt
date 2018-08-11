package dfialho.tveebot.tracker

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.nio.file.Path
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Null

@ConfigurationProperties("tracker")
class TrackerConfig {
}