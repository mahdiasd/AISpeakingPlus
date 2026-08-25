package ir.speaking.core

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.failureRespond
import kotlinx.serialization.SerializationException

fun Application.configureRoutingException() {
    install(StatusPages) {
        exception<AppException> { call, cause ->
            println("configureRoutingException AppException -> $cause.message")
            call.failureRespond(
                cause.httpStatusCode,
                cause.message
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.failureRespond(
                HttpStatusCode.BadRequest,
                message = cause.message ?: "Unknown error"
            )
        }

        exception<BadRequestException> { call, cause ->
            if (cause.message?.contains("Invalid auth header", true) == true)
            {
                call.failureRespond(HttpStatusCode.Unauthorized)
            }else {
                call.failureRespond(
                    HttpStatusCode.BadRequest,
                    message = cause.message ?: "Bad request"
                )
            }
        }

        exception<NotFoundException> { call, cause ->
            call.failureRespond(
                HttpStatusCode.NotFound,
                cause.message ?: "Resource not found"
            )
        }

        exception<SerializationException> { call, cause ->
            call.failureRespond(
                HttpStatusCode.BadRequest,
                "Invalid request format: ${cause.message}"
            )
        }

        exception<Throwable> { call, cause ->
            println("configureRoutingException Throwable -> $cause.message")
            call.failureRespond(
                HttpStatusCode.InternalServerError,
                "Internal server error: ${cause.message}"
            )
        }
    }

}
