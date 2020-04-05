package dfialho.tveebot.tracker.api

import dfialho.tveebot.app.api.models.EpisodeFile
import dfialho.tveebot.utils.Result

/**
 * An episode ledger is an holder for TV show episodes. Episodes can only be appended to a ledger
 * or updated. No episode can be removed from it.
 *
 * The episode ledger is used by the tracker engine to keep track of the episodes that it has
 * already found.
 */
interface EpisodeLedger {

    /**
     * Appends a new episode to the ledger if [episode] is not in the ledger. Otherwise, it tries
     * to update the existing episode if and only if the the [EpisodeFile.publishDate] of the provided [episode]
     * is more recent than the one of the corresponding episode stored on the ledger.
     *
     * @return [Result.Success] if an episode was either updated or appended to the ledger, or a
     * [Result.Failure] if otherwise.
     */
    fun appendOrUpdate(episode: EpisodeFile): Result

    /**
     * Returns a list containing all episode files currently hold by this ledger.
     */
    fun toList(): List<EpisodeFile>
}
