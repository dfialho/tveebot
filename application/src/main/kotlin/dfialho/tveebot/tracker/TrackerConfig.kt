package dfialho.tveebot.tracker

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("tracker")
class TrackerConfig {
    var checkPeriod: Long = 30 // seconds
}