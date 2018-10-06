package dfialho.tveebot.services

import mu.KLogger

/**
 * A service is something that provides some kind of functionality.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
interface Service {

    val name: String

    /**
     * Starts the service.
     */
    fun start()

    /**
     * Stops the service.
     */
    fun stop()
}

inline fun Service.logStart(logger: KLogger, startBody: () -> Unit = {}) {
    logger.debug { "Starting $name" }
    startBody()
    logger.info { "Started $name successfully" }
}

inline fun Service.logStop(logger: KLogger, startBody: () -> Unit = {}) {
    logger.debug { "Stopping $name" }
    startBody()
    logger.info { "Stopped $name successfully" }
}
