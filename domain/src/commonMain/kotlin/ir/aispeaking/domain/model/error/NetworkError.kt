package ir.aispeaking.domain.model.error

sealed class NetworkError(
    open val httpStatus: Int,
    open val message: String
) : AppError {

    data class NotFound(
        override val httpStatus: Int = 404,
        override val message: String = "We can't find what you want."
    ) : NetworkError(httpStatus, message)

    data class InvalidCredentials(
        override val httpStatus: Int = 401,
        override val message: String = "Your name or password is not good. Please try again."
    ) : NetworkError(httpStatus, message)

    data class BadRequest(
        override val httpStatus: Int = 400,
        override val message: String = "Your ask is not good. Please check what you ask."
    ) : NetworkError(httpStatus, message)

    data class Unauthorized(
        override val httpStatus: Int = 401,
        override val message: String = "You can't go here. Please log in first."
    ) : NetworkError(httpStatus, message)

    data class Forbidden(
        override val httpStatus: Int = 403,
        override val message: String = "You can't go here."
    ) : NetworkError(httpStatus, message)

    data class Conflict(
        override val httpStatus: Int = 409,
        override val message: String = "There is a problem with your ask. Please try later."
    ) : NetworkError(httpStatus, message)

    data class Gone(
        override val httpStatus: Int = 410,
        override val message: String = "What you want is not here now."
    ) : NetworkError(httpStatus, message)

    data class UnsupportedMediaType(
        override val httpStatus: Int = 415,
        override val message: String = "The file type you sent is not okay."
    ) : NetworkError(httpStatus, message)

    data class TooManyRequests(
        override val httpStatus: Int = 429,
        override val message: String = "You ask too many times. Please wait a bit."
    ) : NetworkError(httpStatus, message)

    data class ServiceUnavailable(
        override val httpStatus: Int = 503,
        override val message: String = "The service is not working now. Please try later."
    ) : NetworkError(httpStatus, message)

    data class TimeOut(
        override val httpStatus: Int = 408,
        override val message: String = "It looks like the internet is not on. Please check your internet!"
    ) : NetworkError(httpStatus, message)

    data class InternalServer(
        override val httpStatus: Int = 500,
        override val message: String = "There is a problem connecting to the server!"
    ) : NetworkError(httpStatus, message)

    data class Unknown(
        override val httpStatus: Int = -1,
        override val message: String = "A problem happened that we don't know."
    ) : NetworkError(httpStatus, message)
}