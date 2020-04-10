package dfialho.tveebot.app

import dfialho.tveebot.app.services.Service
import org.kodein.di.Kodein
import org.kodein.di.generic.instance


fun notNecessary(): Nothing {
    throw NotImplementedError("An operation is not implemented because it should be required for the tests")
}

inline fun <reified S : Service> startedService(services: Kodein): S {
    val service by services.instance<S>()
    service.start()
    return service
}
