package dfialho.tveebot.downloader

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


/**
 * Controller advice specifying methods to handle exceptions.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@RestControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    /**
     * Error response returned to the user in JSON format.
     *
     * @property exception The base name of the exception which caused the error
     * @property message The error messaging explaining the cause for the error
     */
    private data class ErrorResponse(
        val exception: String,
        val message: String
    )

    /**
     * Handles exceptions which indicate that an element was not found. It returns a response with a NOT FOUND status
     * code.
     */
    @ExceptionHandler(value = [NoSuchElementException::class])
    protected fun handleElementsNotFound(exception: Exception, request: WebRequest): ResponseEntity<Any> {
        val errorResponse = ErrorResponse(
            exception::class.simpleName ?: "",
            exception.message ?: ""
        )

        return handleExceptionInternal(exception, errorResponse, HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }
}