package dfialho.tveebot.utils

sealed class Result {

    abstract val isSuccessful: Boolean
    abstract val isUnsuccessful: Boolean

    object Failure : Result() {
        override val isSuccessful: Boolean get() = false
        override val isUnsuccessful: Boolean get() = true
    }

    object Success : Result() {
        override val isSuccessful: Boolean get() = true
        override val isUnsuccessful: Boolean get() = false
    }
}

val Result.succeeded inline get() = this.isSuccessful
val Result.failed inline get() = this.isUnsuccessful

inline fun <R> Result.ifSucceeded(body: () -> R) {
    if (succeeded) {
        body()
    }
}

inline fun Result.orElse(body: () -> Result): Result {
    return if (failed) {
        return body()
    } else {
        this
    }
}
