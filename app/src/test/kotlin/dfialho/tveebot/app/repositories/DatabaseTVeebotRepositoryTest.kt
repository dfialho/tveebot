package dfialho.tveebot.app.repositories

import assertk.assert
import assertk.assertions.containsExactly
import dfialho.tveebot.app.*
import dfialho.tveebot.app.api.models.*
import dfialho.tveebot.app.services.baseModule
import io.kotest.core.spec.style.FunSpec
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

class DatabaseTVeebotRepositoryTest : FunSpec({

    val services by beforeTestSetup { services() }

    test("find episode files") {

        val tvShow1: TVShow
        val downloadingEpisodeFile: EpisodeFile

        withRepository(services) {

            tvShow1 = anyTVShow().apply {
                upsert(TVShowEntity(this, tracked = true, videoQuality = VideoQuality.FHD))

                val downloadingEpisode = anyEpisode()
                downloadingEpisodeFile = anyEpisodeFile(downloadingEpisode, file = anyVideoFile(VideoQuality.FHD))
                insert(downloadingEpisodeFile)
                update(EpisodeEntity(downloadingEpisode, State.DOWNLOADING))
                insert(anyEpisodeFile(downloadingEpisode, file = anyVideoFile(VideoQuality.HD)))

                insert(anyEpisodeFile(file = anyVideoFile(VideoQuality.FHD)))
                insert(anyEpisodeFile(file = anyVideoFile(VideoQuality.SD)))
                insert(anyEpisodeFile(file = anyVideoFile(VideoQuality.SD)))
            }

            anyTVShow().apply {
                upsert(TVShowEntity(this, tracked = false, videoQuality = VideoQuality.FHD))

                val downloadingEpisode2 = anyEpisode()
                insert(anyEpisodeFile(downloadingEpisode2, file = anyVideoFile(VideoQuality.FHD)))
                update(EpisodeEntity(downloadingEpisode2, State.DOWNLOADING))

                insert(anyEpisodeFile(file = anyVideoFile(VideoQuality.FHD)))
                insert(anyEpisodeFile(file = anyVideoFile(VideoQuality.SD)))
                insert(anyEpisodeFile(file = anyVideoFile(VideoQuality.SD)))
            }
        }

        val files = withRepository(services) {
            findEpisodeFiles(tvShow1.id, State.DOWNLOADING, VideoQuality.FHD)
        }

        assert(files)
            .containsExactly(downloadingEpisodeFile)
    }

    test("find tv shows") {

        val tvShow = anyTVShow()

        withRepository(services) {
            upsert(TVShowEntity(tvShow, tracked = true, videoQuality = VideoQuality.FHD))
            upsert(TVShowEntity(anyTVShow(), tracked = false, videoQuality = VideoQuality.FHD))
            upsert(TVShowEntity(anyTVShow(), tracked = true, videoQuality = VideoQuality.FHD))
        }

        val tvShows = withRepository(services) {
            findTVShows(tracked = true)
        }

        tvShows.forEach { println(it) }
    }
})

private fun services() = Kodein {
    import(baseModule)
    bind<Database>() with singleton { randomInMemoryDatabase() }
}
