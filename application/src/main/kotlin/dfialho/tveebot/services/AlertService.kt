package dfialho.tveebot.services

import mu.KLogging
import java.util.concurrent.ConcurrentHashMap

class AlertService : Service {
    companion object : KLogging()

    override val name: String
        get() = "Alert Service"

    private val handlers: MutableMap<Alert<*>, MutableList<AlertRegistration<*>>> = ConcurrentHashMap()

    private data class AlertRegistration<T>(val service: Service, val handler: AlertHandler<T>)

    override fun start() = logStart(logger)

    override fun stop() = logStop(logger)

    fun <T> subscribe(alert: Alert<T>, service: Service, handler: AlertHandler<T>) {
        handlers.computeIfAbsent(alert) { mutableListOf() }
            .add(AlertRegistration(service, handler))

        logger.debug { "Service '${service.name}' subscribed to '${alert.name}'" }
    }

    fun <T> unsubscribe(alert: Alert<T>, handlingService: Service) {
        handlers[alert]?.removeIf { it.service == handlingService }
    }
    fun <T> raiseAlert(alert: Alert<T>, value: T) {

        handlers[alert]?.forEach { registration ->

            try {
                @Suppress("UNCHECKED_CAST")
                registration.handler as AlertHandler<T>

            } catch (e: ClassCastException) {
                throw RuntimeException("Alert handler was assigned to alert of different value type", e)
            }

            registration.handler.let { handler ->
                registration.service.handler(value)
            }
        }
    }

}

typealias AlertHandler<T> = Service.(T) -> Unit

class Alert<T>(val name: String)

