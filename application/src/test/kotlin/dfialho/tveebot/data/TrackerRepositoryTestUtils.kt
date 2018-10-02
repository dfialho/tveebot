package dfialho.tveebot.data

import dfialho.tveebot.DbSettings

/**
 * Returns an empty [TrackerRepository] that is ready to be used in tests.
 */
fun emptyTrackerRepository(): TrackerRepository {
    return ExposedTrackerRepository(DbSettings.db).apply { clearAll() }
}
