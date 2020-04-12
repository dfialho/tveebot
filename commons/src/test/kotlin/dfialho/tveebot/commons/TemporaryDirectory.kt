package dfialho.tveebot.commons

import io.kotest.core.spec.Spec
import java.nio.file.Path
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class TemporaryDirectory : ReadOnlyProperty<Any?, Path> {

    lateinit var directory: Path

    override fun getValue(thisRef: Any?, property: KProperty<*>): Path {
        return directory
    }
}

fun Spec.temporaryDirectory(): ReadOnlyProperty<Any?, Path> {

    return TemporaryDirectory().apply {
        beforeTest {
            directory = createTempDir().toPath()
        }

        afterTest {
            directory.toFile().deleteRecursively()
        }
    }
}
