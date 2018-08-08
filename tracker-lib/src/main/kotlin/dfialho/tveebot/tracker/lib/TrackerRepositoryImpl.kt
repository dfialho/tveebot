package dfialho.tveebot.tracker.lib

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Repository

@Repository
class TrackerRepositoryImpl : InitializingBean {

    override fun afterPropertiesSet() {
        println("DAVID")
    }
}