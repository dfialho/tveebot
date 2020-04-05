package dfialho.tveebot.utils

sealed class Result {
    object Failure : Result()
    object Success : Result()
}

val Result.succeeded inline get() = this == Result.Success
val Result.failed inline get() = this == Result.Failure

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
