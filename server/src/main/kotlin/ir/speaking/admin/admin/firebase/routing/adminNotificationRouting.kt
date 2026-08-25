package ir.speaking.admin.admin.firebase.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.admin.admin.firebase.dto.SendToTokenRequest
import ir.speaking.admin.admin.firebase.dto.SendToTopicRequest
import ir.speaking.admin.admin.firebase.service.NotificationService
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import org.koin.ktor.ext.inject

fun Application.adminNotificationRouting(
) {
    val notificationService by inject<NotificationService>()

    routing {
        route("/api/v1/admin/notifications") {
            authenticate(MyConstant.ADMIN_JWT_NAME) {
                sendNotificationToToken(notificationService)
                sendNotificationToTopic(notificationService)
                sendNotificationToAll(notificationService)
            }
        }
    }
}

private fun Route.sendNotificationToToken(notificationService: NotificationService) {
    post("/token") {
        val request = call.receive<SendToTokenRequest>()

        notificationService.sendNotificationToToken(
            token = request.token,
            title = request.title,
            body = request.body,
            imageUrl = request.imageUrl
        )

        call.successRespond(
            data = true,
            message = "Notification sent to token successfully."
        )
    }
}


private fun Route.sendNotificationToTopic(notificationService: NotificationService) {
    post("/topic") {
        val request = call.receive<SendToTopicRequest>()

        notificationService.sendNotificationToTopic(
            topic = request.topic,
            title = request.title,
            body = request.body,
            imageUrl = request.imageUrl
        )

        call.successRespond(
            data = true,
            message = "Notification sent to topic '${request.topic}' successfully."
        )
    }
}

private fun Route.sendNotificationToAll(notificationService: NotificationService) {
    post("/all") {
        val request = call.receive<SendToTokenRequest>()

        notificationService.sendNotificationToAllUsers(
            title = request.title,
            body = request.body,
            imageUrl = request.imageUrl
        )

        call.successRespond(
            data = true,
            message = "Notification sent to all users successfully."
        )
    }
}