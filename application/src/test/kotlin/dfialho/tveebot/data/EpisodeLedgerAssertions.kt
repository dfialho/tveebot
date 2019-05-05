package dfialho.tveebot.data

import assertk.Assert
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.doesNotContain
import assertk.assertions.support.expected
import assertk.assertions.support.show
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile

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

/**
 * Asserts the ledger contains all the expected episodes, in any order. The ledger may also contain
 * additional episodes.
 */
fun Assert<EpisodeLedger>.containsAll(vararg episodes: TVShowEpisodeFile) {
    assert(actual.toList()).containsAll(*episodes)
}

/**
 * Asserts the ledger contains all the expected episodes, in any order. The ledger may also contain
 * additional episodes.
 */
fun Assert<EpisodeLedger>.containsAll(episodes: Iterable<TVShowEpisodeFile>) {
    assert(actual.toList()).containsAll(*episodes.toList().toTypedArray())
}

/**
 * Asserts the ledger contains only the expected episodes, in any order. The ledger cannot contain
 * additional episodes.
 */
fun Assert<EpisodeLedger>.containsOnly(vararg episodes: TVShowEpisodeFile) {
    assert(actual.toList()).containsOnly(*episodes)
}

/**
 * Asserts the ledger contains the expected episode, using in.
 */
fun Assert<EpisodeLedger>.contains(episode: TVShowEpisodeFile) {
    assert(actual.toList()).contains(episode)
}

/**
 * Asserts the ledger does not contain the expected episode, using !in.
 */
fun Assert<EpisodeLedger>.doesNotContain(episode: TVShowEpisodeFile) {
    assert(actual.toList()).doesNotContain(episode)
}

/**
 * Returns the number of episodes in the ledger.
 */
val EpisodeLedger.size: Int get() = this.toList().size
