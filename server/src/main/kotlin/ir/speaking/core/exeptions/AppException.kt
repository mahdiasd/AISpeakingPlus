package ir.speaking.core.exeptions

import io.ktor.http.*

sealed class AppException(
    val httpStatusCode: HttpStatusCode,
    override val message: String
) : RuntimeException(message) {

    data class Gone(
        override val message: String = ErrorMessage.GONE_ACTIVE_WORD,
        val statusCode: HttpStatusCode = HttpStatusCode.Gone
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class NotFound(
        override val message: String = ErrorMessage.NOT_FOUND,
        val statusCode: HttpStatusCode = HttpStatusCode.NotFound
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class InvalidMobileNumber(
        override val message: String = ErrorMessage.INVALID_MOBILE_NUMBER,
        val statusCode: HttpStatusCode = HttpStatusCode.BadRequest
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class Conflict(
        override val message: String,
        val statusCode: HttpStatusCode = HttpStatusCode.Conflict
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class UnauthorizedAccess(
        override val message: String = ErrorMessage.DONT_ACCESS,
        val statusCode: HttpStatusCode = HttpStatusCode.Unauthorized
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class BadRequest(
        override val message: String = "Invalid request parameters",
        val statusCode: HttpStatusCode = HttpStatusCode.BadRequest
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class InvalidOtpCode(
        override val message: String = ErrorMessage.OTP_CODE_NOT_CORRECT,
        val statusCode: HttpStatusCode = HttpStatusCode.Forbidden
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class UnknownError(
        override val message: String,
        val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
    ) : AppException(httpStatusCode = statusCode, message = message)

    data class NoActiveSubscription(
        override val message: String = ErrorMessage.NO_ACTIVE_SUBSCRIPTION,
        val statusCode: HttpStatusCode = HttpStatusCode.PaymentRequired
    ) : AppException(httpStatusCode = statusCode, message = message)
}