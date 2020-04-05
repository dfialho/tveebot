package dfialho.tveebot.app

import org.jetbrains.exposed.sql.Database
import java.util.*

fun withDatabase(block: (Database) -> Unit) {

    val dbName = UUID.randomUUID().toString()
    val db = Database.connect(
        url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )

    block(db)
}
