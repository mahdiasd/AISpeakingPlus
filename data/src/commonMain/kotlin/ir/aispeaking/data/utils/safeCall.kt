package ir.aispeaking.data.utils

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import ir.aispeaking.data.mapper.paginate.toDomain
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.AppError
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.utils.dLog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


private val safeCallJson = Json { ignoreUnknownKeys = true }

suspend fun <T> safeCall(execute: suspend () -> NetworkResponse<T>): DataResult<T> {
    return try {
        val response = execute.invoke()
        when {
            response.data != null -> {
                DataResult.Success(
                    data = response.data!!,
                    pagingMeta = response.pagingMetaResponse?.toDomain(),
                    message = response.message ?: ""
                )
            }

            response.errorCode != null -> {
                val error = getApiError(
                    statusCode = response.errorCode!!,
                    message = response.errorMessage ?: ""
                )
                DataResult.Failure(error)
            }

            else -> {
                DataResult.Failure(
                    NetworkError.Unknown(message = response.errorMessage ?: "Bad Json Template!")
                )
            }
        }
    } catch (e: ResponseException) {
        val statusCode = e.response.status.value
        val serverErrorMessage = try {
            val text = e.response.bodyAsText()
            try {
                val parsed = safeCallJson.decodeFromString<NetworkResponse<JsonElement?>>(text)
                parsed.errorMessage?.takeIf { it.isNotBlank() } ?: parsed.message?.takeIf { it.isNotBlank() } ?: text
            } catch (_: Throwable) {
                text.takeIf { it.isNotBlank() }
            }
        } catch (_: Throwable) {
            null
        } ?: e.message ?: ""
        serverErrorMessage.dLog(tag = "ktor", plusTag = "safeCall ResponseException ($statusCode): ")
        DataResult.Failure(getApiError(statusCode, serverErrorMessage))
    } catch (e: Throwable) {
        e.message.dLog(tag = "ktor", plusTag = "safeCall: ")

        DataResult.Failure(getCatchError(e.message))
    }
}

fun getCatchError(message: String?): AppError {
    return if (message?.contains("timeout", true) == true) {
        NetworkError.TimeOut(httpStatus = 500)
    } else
        NetworkError.InternalServer(500)
}

fun getApiError(statusCode: Int, message: String): AppError {
    return when (statusCode) {
        400 -> NetworkError.BadRequest(httpStatus = statusCode, message = message)
        401 -> NetworkError.Unauthorized(httpStatus = statusCode, message = message)
        402 -> NetworkError.PaymentRequired(httpStatus = statusCode, message = message)
        403 -> NetworkError.Forbidden(httpStatus = statusCode, message = message)
        404 -> NetworkError.NotFound(httpStatus = statusCode, message = message)
        409 -> NetworkError.Conflict(httpStatus = statusCode, message = message)
        410 -> NetworkError.Gone(httpStatus = statusCode, message = message)
        415 -> NetworkError.UnsupportedMediaType(httpStatus = statusCode, message = message)
        429 -> NetworkError.TooManyRequests(httpStatus = statusCode, message = message)
        503 -> NetworkError.ServiceUnavailable(httpStatus = statusCode, message = message)
        else -> NetworkError.Unknown(httpStatus = statusCode, message = message)
    }
}