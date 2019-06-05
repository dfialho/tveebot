package dfialho.tveebot.application.api

import java.util.*

data class ID(val value: String)

fun randomID(): ID = ID(UUID.randomUUID().toString())
