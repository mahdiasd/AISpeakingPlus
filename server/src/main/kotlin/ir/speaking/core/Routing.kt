package ir.speaking.core

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ir.speaking.admin.admin.firebase.routing.adminNotificationRouting
import ir.speaking.admin.admin.routing.adminRouting
import ir.speaking.admin.message.adminMessageRouting
import ir.speaking.feature.chat.routing.chatRouting
import ir.speaking.feature.config.configRouting
import ir.speaking.feature.stt.routing.sttRouting
import ir.speaking.feature.tts.routing.ttsRouting
import ir.speaking.feature.user.routing.userRouting

fun Application.configureRouting() {
    routing {
        staticResources("/resources", "static")

        // Unauthenticated health endpoint for Docker healthcheck and load balancers.
        // Returns 200 OK with a simple JSON body once the app has started.
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }
    }
    adminRouting()
    adminNotificationRouting()
    adminMessageRouting()

    configRouting()
    userRouting()
    chatRouting()

    sttRouting()
    ttsRouting()
}