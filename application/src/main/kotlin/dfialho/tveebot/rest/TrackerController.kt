package dfialho.tveebot.rest

import dfialho.tveebot.services.tracker.TrackerService
import dfialho.tveebot.tracker.api.TVShow
import dfialho.tveebot.tracker.api.TrackedTVShow
import dfialho.tveebot.tracker.api.VideoQuality
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    @GetMapping("tvshows/not-tracked")
    fun getNotTrackedTVShows(): List<TVShow> = trackerService.getNotTrackedTVShows()

    /**
     * Tells this tracker service to start tracking the TV show identified by [uuid]. Downloaded episode files for
     * this TV show must be of the specified video [quality].
     *
     * The video quality, which must be one of:
     *  - "FULL_HD" (1080p)
     *  - "HD" (720p)
     *  - "SD" (480p)
     */
    @PutMapping("tvshow/{uuid}")
    fun addTVShow(
        @PathVariable uuid: UUID,
        @RequestParam quality: VideoQuality?
    ) {
        trackerService.trackTVShow(uuid, videoQuality = quality ?: VideoQuality.HD)
    }

    /**
     * Removes the TV show with the given UUID from the set of tracked TV shows.
     */
    @DeleteMapping("tvshow/{uuid}")
    fun removeTVShow(@PathVariable uuid: UUID) {
        trackerService.untrackTVShow(uuid)
    }

    /**
     * Sets the video [quality] of episode files corresponding to the TV show identified by [uuid].
     *
     * The video quality, which must be one of:
     *  - "FULL_HD" (1080p)
     *  - "HD" (720p)
     *  - "SD" (480p)
     */
    @PostMapping("tvshow/{uuid}")
    fun setTVShowVideoQuality(
        @PathVariable uuid: UUID,
        @RequestParam quality: VideoQuality
    ) {
        trackerService.setTVShowVideoQuality(uuid, quality)
    }
}