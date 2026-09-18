package ir.speaking.core

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ir.speaking.feature.stt.routing.sttRouting
import ir.speaking.feature.tts.routing.ttsRouting

fun Application.configureRouting() {
    routing {
        staticResources("/resources", "static")

        // Unauthenticated health endpoint for Docker healthcheck and load balancers.
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }
    }

    sttRouting()
    ttsRouting()
}