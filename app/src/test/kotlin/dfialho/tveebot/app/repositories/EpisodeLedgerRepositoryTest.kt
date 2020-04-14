package dfialho.tveebot.app.repositories

import assertk.assert
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import dfialho.tveebot.app.api.models.*
import dfialho.tveebot.app.randomInMemoryDatabase
import io.kotest.core.spec.style.FunSpec
import java.time.Instant
import java.time.temporal.ChronoUnit

class EpisodeLedgerRepositoryTest : FunSpec({

    val tvShow = anyTVShow()

    fun createEmptyLedger(): EpisodeLedgerRepository {

        val repository = DatabaseTVeebotRepository(randomInMemoryDatabase())
        repository.upsert(TVShowEntity(tvShow, tracked = true))

        return EpisodeLedgerRepository(repository)
    }

    test("when the new episode comes then it is appended") {

        val ledger = createEmptyLedger()
        val episodeFile = anyEpisodeFile(tvShow)

        assert(ledger.appendOrUpdate(episodeFile))
            .isTrue()
    }

    test("when the same episode comes then it is NOT appended") {

        val ledger = createEmptyLedger()
        val episodeFile = anyEpisodeFile(tvShow)

        ledger.appendOrUpdate(episodeFile)

        assert(ledger.appendOrUpdate(episodeFile))
            .isFalse()
    }

    test("when another episode file comes that is more recent it is appended") {

        val ledger = createEmptyLedger()
        val instant = Instant.now()
        val oldEpisodeFile = anyEpisodeFile(tvShow, anyVideoFile(publishedDate = instant.minus(1, ChronoUnit.DAYS)))
        val newEpisodeFile = oldEpisodeFile.copy(file = oldEpisodeFile.file.copy(publishDate = instant))

        ledger.appendOrUpdate(oldEpisodeFile)

        assert(ledger.appendOrUpdate(newEpisodeFile))
            .isTrue()
    }

    test("when another episode file comes that is older it is NOT appended") {

        val ledger = createEmptyLedger()
        val instant = Instant.now()
        val oldEpisodeFile = anyEpisodeFile(tvShow, anyVideoFile(publishedDate = instant.minus(1, ChronoUnit.DAYS)))
        val newEpisodeFile = oldEpisodeFile.copy(file = oldEpisodeFile.file.copy(publishDate = instant))

        ledger.appendOrUpdate(newEpisodeFile)

        assert(ledger.appendOrUpdate(oldEpisodeFile))
            .isFalse()
    }

    test("when an episode file with a different quality comes it is appended") {

        val ledger = createEmptyLedger()
        val instant = Instant.now()
        val episodeFile720p = anyEpisodeFile(tvShow, anyVideoFile(publishedDate = instant.minus(1, ChronoUnit.DAYS)))
        val episodeFile480p = episodeFile720p.copy(file = episodeFile720p.file.copy(quality = VideoQuality.SD, publishDate = instant))

        ledger.appendOrUpdate(episodeFile720p)

        assert(ledger.appendOrUpdate(episodeFile480p))
            .isTrue()
    }
})
