package dfialho.tveebot.app

import dfialho.tveebot.app.repositories.DatabaseTVeebotRepository
import dfialho.tveebot.app.repositories.TVeebotRepository
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.*

fun newRepository(): TVeebotRepository {

    val dbName = UUID.randomUUID().toString()
    val db = Database.connect(
        url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )
    return DatabaseTVeebotRepository(db)
}

fun <R> withRepository(services: Kodein, block: TVeebotRepository.() -> R): R {

    val repository by services.instance<TVeebotRepository>()
    return with(repository, block)
}
