package dfialho.tveebot.exceptions

/**
 * Thrown to indicate the a certain expected element was not found.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class NotFoundException(message: String, throwable: Throwable? = null)
    : RuntimeException(message, throwable)
