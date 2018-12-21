package dfialho.tveebot.testing

import dfialho.tveebot.data.ExposedTrackerRepository
import dfialho.tveebot.data.TrackerRepository
import org.jetbrains.exposed.sql.Database
import java.util.UUID.randomUUID

/**
 * Creates a new [TrackerRepository] for testing purposes each time this method is called.
 */
fun newRepository(): TrackerRepository {
    val dbName = randomUUID().toString()
    val db = Database.connect("jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", driver = "org.h2.Driver")

    return ExposedTrackerRepository(db).apply { clearAll() }
}