package dfialho.tveebot.app

import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.events.subscribe
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class EventRecorder(eventBus: EventBus) {

    private val firedEvents = LinkedBlockingQueue<Event.EpisodeFileFound>()

    init {
        subscribe<Event.EpisodeFileFound>(eventBus) {
            firedEvents.add(it)
        }
    }

    fun waitForEvent(): Event.EpisodeFileFound? {
        return firedEvents.poll(100, TimeUnit.MILLISECONDS)
    }

    fun waitForEvents(count: Int): List<Event.EpisodeFileFound> {

        return (0..count)
            .mapNotNull { firedEvents.poll(100, TimeUnit.MILLISECONDS) }
    }
}
