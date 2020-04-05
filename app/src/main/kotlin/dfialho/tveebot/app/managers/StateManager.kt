package dfialho.tveebot.app.managers

import dfialho.tveebot.app.api.models.State

interface StateManager {
    fun get(id: String): State
    fun set(id: String, state: State)
}