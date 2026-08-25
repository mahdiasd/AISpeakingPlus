package ir.speaking.core.response

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ir.speaking.core.utils.toJsonElement
import kotlinx.serialization.Serializable

@Serializable
data class SuccessResponse<T>(
    val data: T?,
    val message: String = "",
    val pagingMeta: PagingMeta? = null
)

@Serializable
data class FailureResponse(
    val errorCode: Int,
    val errorMessage: String,
)

suspend inline fun <reified T> RoutingCall.successRespond(
    data: T,
    pagingMeta: PagingMeta? = null,
    message: String = ""
) {
    this.respond(
        HttpStatusCode.OK, SuccessResponse(data.toJsonElement(), pagingMeta = pagingMeta, message = message),
    )
}

/**
 * ------------------------- Failure Responses --------------------------------------------
 * */

suspend fun ApplicationCall.failureRespond(httpStatusCode: HttpStatusCode) {
    this.respond(httpStatusCode, FailureResponse(httpStatusCode.value, httpStatusCode.getMessage()))
}

suspend fun ApplicationCall.failureRespond(httpStatusCode: HttpStatusCode, message: String) {
    this.respond(httpStatusCode, FailureResponse(httpStatusCode.value, message))
}

