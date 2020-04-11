package dfialho.tveebot.app

import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class EventRecorder<E : Event> {

    private val firedEvents = LinkedBlockingQueue<E>()

    fun waitForEvent(timeout: Duration = Duration.ofMillis(100)): E? {
        return firedEvents.poll(timeout.toMillis(), TimeUnit.MILLISECONDS)
    }

    fun waitForEvents(count: Int, timeout: Duration = Duration.ofMillis(100)): List<E> {

        return (1..count)
            .mapNotNull { firedEvents.poll(timeout.toMillis(), TimeUnit.MILLISECONDS) }
    }

    fun onEvent(event: E) {
        firedEvents.add(event)
    }
}

inline fun <reified E : Event> recordEvents(services: Kodein): EventRecorder<E> {

    val eventBus by services.instance<EventBus>()

    val eventRecorder = EventRecorder<E>()
    eventRecorder.subscribe<E>(eventBus) {
        eventRecorder.onEvent(it)
    }

    return eventRecorder
}
