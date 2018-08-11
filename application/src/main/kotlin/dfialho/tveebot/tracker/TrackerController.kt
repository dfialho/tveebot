package dfialho.tveebot.tracker

import dfialho.tveebot.tracker.api.TVShow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("tracker")
class TrackerController(private val trackerService: TrackerService) {

    @GetMapping("tvshows")
    fun getTVShows(@RequestParam("tracked") tracked: Boolean = true): List<TVShow> {
        return trackerService.getTVShows(tracked)
    }

    @PutMapping("tvshow/add/{uuid}")
    fun startTrackingTVShow(@PathVariable("uuid") tvShowUUID: UUID) {
        trackerService.setTVShowTracked(tvShowUUID, tracked = true)
    }

    @PutMapping("tvshow/remove/{uuid}")
    fun stopTrackingTVShow(@PathVariable("uuid") tvShowUUID: UUID) {
        trackerService.setTVShowTracked(tvShowUUID, tracked = false)
    }
}