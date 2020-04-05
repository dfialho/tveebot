package dfialho.tveebot.app.events

import dfialho.tveebot.app.services.Service

inline fun <reified E : Event> Service.subscribe(eventBus: EventBus, noinline handler: (E) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    eventBus.subscribe(E::class, this, handler as (Event) -> Unit)
}

inline fun <reified E : Event> Service.unsubscribe(eventBus: EventBus) {
    eventBus.unsubscribe(E::class, this)
}

inline fun <reified E : Event> fire(eventBus: EventBus, event: E) {
    eventBus.fire(event)
}
