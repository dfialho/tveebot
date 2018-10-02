package dfialho.tveebot.services

/**
 * A service is something that provides some kind of functionality.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface Service {

    /**
     * Starts the service.
     */
    fun start()

    /**
     * Stops the service.
     */
    fun stop()
}