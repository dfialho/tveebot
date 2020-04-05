package dfialho.tveebot.app.events

import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class EventBus {
    companion object : KLogging()

    private val handlers: MutableMap<KClass<out Event>, MutableList<EventRegistration>> = ConcurrentHashMap()

    private data class EventRegistration(val service: Any, val handler: (Event) -> Unit)

    fun <T : Event> subscribe(service: Any, eventType: KClass<out T>, handler: (Event) -> Unit) {
        handlers.computeIfAbsent(eventType) { mutableListOf() }
            .add(EventRegistration(service, handler))
    }

    fun <T : Event> unsubscribe(handlingService: Any, eventType: KClass<out T>) {
        handlers[eventType]?.removeIf { it.service === handlingService }
    }

    fun <T : Event> fire(event: T) {

        handlers[event::class]?.forEach { registration ->
            registration.handler(event)
        }
    }
}

inline fun <reified E : Event> Any.subscribe(eventBus: EventBus, noinline handler: (E) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    eventBus.subscribe(this, E::class, handler as (Event) -> Unit)
}

inline fun <reified E : Event> Any.unsubscribe(eventBus: EventBus) {
    eventBus.unsubscribe(this, E::class)
}

inline fun <reified E : Event> fire(eventBus: EventBus, event: E) {
    eventBus.fire(event)
}
