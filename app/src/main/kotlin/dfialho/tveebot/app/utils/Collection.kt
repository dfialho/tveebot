package dfialho.tveebot.app.utils

import java.util.concurrent.ConcurrentHashMap

fun <K, V> Map<out K, V>.toConcurrentHashMap(): ConcurrentHashMap<K, V> {
    return ConcurrentHashMap(this)
}
