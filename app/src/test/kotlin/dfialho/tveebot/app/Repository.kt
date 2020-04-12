package dfialho.tveebot.app

import dfialho.tveebot.app.repositories.TVeebotRepository
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun randomInMemoryDatabase(): Database {

    val dbName = UUID.randomUUID().toString()

    return Database.connect(
        url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )
}

@OptIn(ExperimentalContracts::class)
inline fun <R> withRepository(services: Kodein, block: TVeebotRepository.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val repository by services.instance<TVeebotRepository>()
    return with(repository, block)
}
