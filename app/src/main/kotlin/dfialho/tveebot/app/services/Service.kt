package dfialho.tveebot.app.services

/**
 * A service is something that provides some kind of functionality.
 */
interface Service {

    /**
     * Name of the service.
     */
    val name: String
        get() = this.javaClass.simpleName

    /**
     * Starts the service.
     */
    fun start()

    /**
     * Stops the service.
     */
    fun stop()
}
