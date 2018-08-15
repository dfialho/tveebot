package dfialho.tveebot.tracker

import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackedTVShow
import dfialho.tveebot.tracker.api.VideoQuality
import dfialho.tveebot.tracker.api.toVideoQuality
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Rest controller for the [TrackerService].
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@RestController
@RequestMapping("tracker")
class TrackerController(private val trackerService: TrackerService) {

    /**
     * Retrieves all TV shows currently being tracked.
     */
    @GetMapping("tvshows/tracked")
    fun getTrackedTVShows(): List<TrackedTVShow> = trackerService.getTrackedTVShows()

    /**
     * Retrieves all TV shows currently NOT being tracked.
     */
    @GetMapping("tvshows/nottracked")
    fun getNotTrackedTVShows(): List<TVShow> = trackerService.getNotTrackedTVShows()

    /**
     * Tells this tracker service to start tracking the TV show identified by [uuid]. Downloaded episode files for
     * this TV show must be of the specified video [quality].
     *
     * The video quality parameter should be one of:
     *  - 1080p
     *  - 720p (default)
     *  - 480p
     */
    @PutMapping("tvshow/track/{uuid}")
    fun trackTVShow(
        @PathVariable() uuid: UUID,
        @RequestParam quality: String = VideoQuality.HD.identifier
    ) {
        trackerService.trackTVShow(uuid, quality.toVideoQuality())
    }

    /**
     * Removes the TV show with the given UUID from the set of tracked TV shows.
     */
    @DeleteMapping("tvshow/remove/{uuid}")
    fun untrackTVShow(@PathVariable("uuid") tvShowUUID: UUID) {
        trackerService.untrackTVShow(tvShowUUID)
    }
}