package dfialho.tveebot.app.managers

import assertk.assert
import assertk.assertions.isEqualTo
import dfialho.tveebot.app.api.models.State
import dfialho.tveebot.app.withDatabase
import io.kotlintest.specs.FunSpec

class StateManagerRepositoryTest : FunSpec({

    test("by default the state is ${State.FOUND}") {
        withDatabase { db ->
            val stateManager = StateManagerRepository(db)
            val episodeId = "id"

            assert(stateManager.get(episodeId))
                .isEqualTo(State.FOUND)
        }
    }

    test("setting the state for the first time") {
        withDatabase { db ->
            val stateManager = StateManagerRepository(db)
            val episodeId = "id"
            val setState = State.DOWNLOADING

            stateManager.set(episodeId, setState)

            assert(stateManager.get(episodeId))
                .isEqualTo(setState)
        }
    }

    test("setting the state for the second time") {
        withDatabase { db ->
            val stateManager = StateManagerRepository(db)
            val episodeId = "id"

            stateManager.set(episodeId, State.DOWNLOADING)
            stateManager.set(episodeId, State.DOWNLOADED)

            assert(stateManager.get(episodeId))
                .isEqualTo(State.DOWNLOADED)
        }
    }
})
