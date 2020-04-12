package dfialho.tveebot.app.services

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dfialho.tveebot.app.api.models.*
import dfialho.tveebot.app.beforeTestSetup
import dfialho.tveebot.app.events.Event
import dfialho.tveebot.app.events.EventBus
import dfialho.tveebot.app.randomInMemoryDatabase
import dfialho.tveebot.app.start
import dfialho.tveebot.app.withRepository
import io.kotest.core.spec.style.FunSpec
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.nio.file.Paths

class StateServiceTest : FunSpec({

    val services by beforeTestSetup { services() }
    beforeTest { start<StateService>(services) }

    fun <E : Event> test(event: (EpisodeFile) -> E, state: State) {

        test("when a $event is fired then the state is updated to $state") {

            val tvShow = TVShowEntity(anyTVShow(), tracked = true)
            val episodeFile = anyEpisodeFile(tvShow.tvShow)
            withRepository(services) {
                upsert(tvShow)
                insert(episodeFile)
            }
            val eventBus by services.instance<EventBus>()

            eventBus.fire(event(episodeFile))

            val episode = withRepository(services) { findEpisode(episodeFile.episodes[0].id) }
            assert(episode?.state)
                .isEqualTo(state)
        }
    }

    test(
        event = { file -> Event.EpisodeFileFound(file) },
        state = State.FOUND
    )

    test(
        event = { file -> Event.DownloadStarted(file) },
        state = State.DOWNLOADING
    )

    test(
        event = { file -> Event.DownloadFinished(file, Paths.get("video.mkv")) },
        state = State.DOWNLOADED
    )

    test(
        event = { file -> Event.FileStored(file, Paths.get("library/video.mkv")) },
        state = State.STORED
    )

    test("when the episode is not in the repository future events are still updated") {

        val tvShow = TVShowEntity(anyTVShow(), tracked = true)
        val missingEpisodeFile = anyEpisodeFile(tvShow.tvShow)
        val existingEpisodeFile = anyEpisodeFile(tvShow.tvShow)
        withRepository(services) {
            upsert(tvShow)
            insert(existingEpisodeFile)
        }
        val eventBus by services.instance<EventBus>()

        eventBus.fire(Event.DownloadStarted(missingEpisodeFile))
        eventBus.fire(Event.DownloadStarted(existingEpisodeFile))

        assertAll {
            val missingEpisode = withRepository(services) { findEpisode(missingEpisodeFile.episodes[0].id) }
            assert(missingEpisode)
                .isNull()

            val existingEpisode = withRepository(services) { findEpisode(existingEpisodeFile.episodes[0].id) }
            assert(existingEpisode?.state)
                .isEqualTo(State.DOWNLOADING)
        }
    }
})

private fun services() = Kodein {
    importOnce(stateModule)
    bind<Database>() with singleton { randomInMemoryDatabase() }
}
