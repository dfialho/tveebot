package dfialho.tveebot.tracker.api.models

import java.util.*

data class ID(val value: String)

fun randomID(): ID = ID(UUID.randomUUID().toString())
