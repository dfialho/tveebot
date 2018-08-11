package dfialho.tveebot.tracker

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tracker")
class TrackerController(private val trackerService: TrackerService) {

//    @PutMapping("/tvshow/{uuid}")
//    fun putTVShow(@PathVariable("uuid") tvShow: TVShow) {
//        trackerService.repository.put()
//    }
}