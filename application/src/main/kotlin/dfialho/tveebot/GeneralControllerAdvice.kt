package dfialho.tveebot

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


/**
 * Controller advice specifying methods to handle exceptions.
 *
 * @author David Fialho (dfialho@protonmail.com)
 */
@RestControllerAdvice
class GeneralControllerAdvice : ResponseEntityExceptionHandler() {

    /**
     * Error response returned to the user in JSON format.
     *
     * @property exception The base name of the exception which caused the error
     * @property message The error messaging explaining the cause for the error
     */
    private data class ErrorResponse(private val exception: Exception, private val prefix: String = "") {
        val error: String get() = exception::class.simpleName ?: ""
        val message: String get() = prefix + exception.message
    }

    /**
     * Handles exceptions which indicate that an element was not found. It returns a response with a NOT FOUND status
     * code.
     */
    @ExceptionHandler(value = [NoSuchElementException::class])
    protected fun handleElementsNotFound(exception: Exception, request: WebRequest): ResponseEntity<Any> {
        return handleExceptionInternal(exception, ErrorResponse(exception), HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }

    /**
     * Handles exceptions which indicate that the input parameters are not valid. It returns a response with a
     * BAD REQUEST status code.
     */
    @ExceptionHandler(value = [IllegalArgumentException::class])
    protected fun handleInvalidArguments(exception: Exception, request: WebRequest): ResponseEntity<Any> {
        val errorResponse = ErrorResponse(exception, prefix = "Invalid parameter: ")
        return handleExceptionInternal(exception, errorResponse, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }
}