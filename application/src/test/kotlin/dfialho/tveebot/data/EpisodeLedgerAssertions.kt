package dfialho.tveebot.data

import assertk.Assert
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.doesNotContain
import dfialho.tveebot.assertions.containsOnly
import dfialho.tveebot.tracker.api.EpisodeLedger
import dfialho.tveebot.tracker.api.models.TVShowEpisodeFile

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
