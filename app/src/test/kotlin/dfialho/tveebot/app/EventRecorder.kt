package dfialho.tveebot.app

import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class EventRecorder<E : Event>() {

    private val firedEvents = LinkedBlockingQueue<E>()

    fun waitForEvent(): E? {
        return firedEvents.poll(100, TimeUnit.MILLISECONDS)
    }

    fun waitForEvents(count: Int): List<E> {
        return (0..count)
            .mapNotNull { firedEvents.poll(100, TimeUnit.MILLISECONDS) }
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
