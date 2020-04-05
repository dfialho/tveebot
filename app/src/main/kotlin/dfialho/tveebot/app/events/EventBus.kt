package dfialho.tveebot.app.events

import dfialho.tveebot.app.services.Service
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class EventBus {
    companion object : KLogging()

    private val handlers: MutableMap<KClass<out Event>, MutableList<EventRegistration>> = ConcurrentHashMap()

    private data class EventRegistration(val service: Service, val handler: (Event) -> Unit)

    fun <T : Event> subscribe(eventType: KClass<out T>, service: Service, handler: (Event) -> Unit) {
        handlers.computeIfAbsent(eventType) { mutableListOf() }
            .add(EventRegistration(service, handler))

        logger.debug { "Service '${service.name}' subscribed to '${eventType.simpleName}'" }
    }

    fun <T : Event> unsubscribe(eventType: KClass<out T>, handlingService: Service) {
        handlers[eventType]?.removeIf { it.service === handlingService }
        logger.debug { "Service '${handlingService.name}' unsubscribed from '${eventType.simpleName}'" }
    }

    fun <T : Event> fire(event: T) {

        handlers[event::class]?.forEach { registration ->
            registration.handler(event)
        }
    }
}
