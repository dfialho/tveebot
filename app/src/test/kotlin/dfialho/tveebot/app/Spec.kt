package dfialho.tveebot.app

import dfialho.tveebot.app.services.Service
import io.kotest.core.spec.Spec
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified S : Service> Spec.start(services: Kodein): S {
    val service by services.instance<S>()
    service.start()
    afterTest { service.stop() }
    return service
}

fun <T : Any> Spec.beforeTestSetup(setup: () -> T): ReadOnlyProperty<Any?, T> {

    return BeforeTestProperty(setup).apply {
        beforeTest {
            reset()
        }
    }
}

private class BeforeTestProperty<T : Any>(private val build: () -> T) : ReadOnlyProperty<Any?, T> {

    private lateinit var value: T

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    fun reset() {
        value = build()
    }
}
