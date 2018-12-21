package dfialho.tveebot.assertions

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show

/**
 * Asserts the collection contains only the expected [elements], in any order. The collection cannot
 * contain additional elements.
 */
fun <E, T : Collection<E>> Assert<T>.containsOnly(vararg elements: E) {

    val missingElements = actual.filterNot { it in elements }
    val unexpectedElements = elements.filterNot { it in actual }

    if (missingElements.isEmpty() and unexpectedElements.isEmpty()) {
        return
    }

    var message = "to contain only:${show(elements)}"
    if (missingElements.isNotEmpty()) {
        message += " some elements were not found:${show(missingElements)}"
    }
    if (unexpectedElements.isNotEmpty()) {
        message += " some elements were not expected:${show(unexpectedElements)}"
    }

    expected(message)
}