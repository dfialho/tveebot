package dfialho.tveebot.tracker

import dfialho.tveebot.tracker.api.TVShow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("tracker")
class TrackerController(private val trackerService: TrackerService) {

    @GetMapping("tvshows")
    fun putTVShow(@RequestParam("tracked") tracked: Boolean = true): List<TVShow> {
        return trackerService.getTVShows(tracked)
    }
}