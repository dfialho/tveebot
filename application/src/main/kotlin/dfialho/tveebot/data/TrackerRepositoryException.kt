package dfialho.tveebot.data


/**
 * Thrown by [TrackerRepository] to indicate an error while trying to perform some operation in the repository, which
 * prevents the operation from preceding.
 */
class TrackerRepositoryException(message: String, throwable: Throwable? = null) : Exception(message, throwable)
