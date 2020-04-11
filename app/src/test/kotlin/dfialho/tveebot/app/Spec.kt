package dfialho.tveebot.app

import dfialho.tveebot.app.services.Service
import io.kotest.core.spec.Spec
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

inline fun <reified S : Service> Spec.startedService(services: Kodein): S {
    val service by services.instance<S>()
    service.start()
    afterTest { service.stop() }
    return service
}
