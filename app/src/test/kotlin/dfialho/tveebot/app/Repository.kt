package dfialho.tveebot.app

import dfialho.tveebot.app.repositories.DatabaseTVeebotRepository
import dfialho.tveebot.app.repositories.TVeebotRepository
import org.jetbrains.exposed.sql.Database
import java.util.*

fun newRepository(): TVeebotRepository {

    val dbName = UUID.randomUUID().toString()
    val db = Database.connect(
        url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )
    return DatabaseTVeebotRepository(db)
}
