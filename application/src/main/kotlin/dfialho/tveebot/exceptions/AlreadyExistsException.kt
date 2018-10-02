package dfialho.tveebot.exceptions

/**
 * Thrown to indicate that an element that was expected to not exist, unexpectedly, exists.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
class AlreadyExistsException(message: String, throwable: Throwable? = null)
    : RuntimeException(message, throwable)