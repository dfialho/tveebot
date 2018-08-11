package dfialho.tveebot.tracker

import dfialho.tveebot.tracker.api.TVShow
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
     * According to [tracked], retrieves either every TV show currently being tracked, or every TV show not being
     * tracked.
     */
    @GetMapping("tvshows")
    fun getTVShows(@RequestParam("tracked") tracked: Boolean = true): List<TVShow> {
        return trackerService.getTVShows(tracked)
    }

    /**
     * Adds the TV show with the given UUID to the set of tracked TV shows.
     */
    @PutMapping("tvshow/add/{uuid}")
    fun startTrackingTVShow(@PathVariable("uuid") tvShowUUID: UUID) {
        trackerService.setTVShowTracked(tvShowUUID, tracked = true)
    }

    /**
     * Removes the TV show with the given UUID from the set of tracked TV shows.
     */
    @DeleteMapping("tvshow/remove/{uuid}")
    fun stopTrackingTVShow(@PathVariable("uuid") tvShowUUID: UUID) {
        trackerService.setTVShowTracked(tvShowUUID, tracked = false)
    }
}