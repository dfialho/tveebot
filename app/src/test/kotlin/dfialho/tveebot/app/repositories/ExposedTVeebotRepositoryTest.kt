package dfialho.tveebot.app.repositories

import assertk.assert
import dfialho.tveebot.app.anyTVShow
import dfialho.tveebot.app.withRepository
import io.kotlintest.specs.FunSpec

class ExposedTVeebotRepositoryTest : FunSpec({

    test("after storing a tv show it is listed") {
        withRepository { repository ->
            val tvShow = anyTVShow()

            repository.store(tvShow)

//            assert(repository.listTVShows())
//                .containsExactly(tvShow)
        }
    }

    test("trying to store an existing tv show does not fail") {
        withRepository { repository ->
            val tvShow = anyTVShow()
            repository.store(tvShow)

            assert { repository.store(tvShow) }
                .doesNotThrowAnyException()
        }
    }

//    test("after storing an episode from a non-existing tv show") {
//        withRepository { repository ->
//            val tvShow = anyTVShow()
//
//            repository.store(tvShow)
//
//            assert(repository.listEpisodes())
//                .containsExactly(tvShow)
//        }
//    }
})
