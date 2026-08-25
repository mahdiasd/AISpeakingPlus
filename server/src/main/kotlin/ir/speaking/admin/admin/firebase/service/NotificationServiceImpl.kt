package ir.speaking.admin.admin.firebase.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import ir.speaking.core.exeptions.AppException
import ir.speaking.feature.user_device_info.repository.UserDeviceInfoRepository
import org.koin.core.annotation.Single

@Single
class NotificationServiceImpl(
    private val userDeviceInfoRepository: UserDeviceInfoRepository
) : NotificationService {

    override fun sendNotificationToToken(token: String, title: String, body: String, imageUrl: String?) {
        if (token.isBlank()) {
            println("Firebase token is blank, skipping.")
            return
        }

        val notificationBuilder = Notification.builder()
            .setTitle(title)
            .setBody(body)

        if (!imageUrl.isNullOrBlank()) {
            notificationBuilder.setImage(imageUrl)
        }

        val message = Message.builder()
            .setNotification(notificationBuilder.build())
            .putData("custom_key", "custom_value")
            .setToken(token)

            .build()

        try {
            FirebaseMessaging.getInstance().send(message)
        } catch (e: FirebaseMessagingException) {
            println("Firebase Error: ${e.messagingErrorCode} - ${e.message}")
//            throw AppException.BadRequest("Failed to send notification: ${e.message}")
        }
    }

    override fun sendNotificationToTopic(topic: String, title: String, body: String, imageUrl: String?) {
        val notificationBuilder = Notification.builder()
            .setTitle(title)
            .setBody(body)

        if (!imageUrl.isNullOrBlank()) {
            notificationBuilder.setImage(imageUrl)
        }

        val message = Message.builder()
            .setNotification(notificationBuilder.build())
            .setTopic(topic)
            .build()

        try {
            FirebaseMessaging.getInstance().send(message)
        } catch (e: FirebaseMessagingException) {
            println("Firebase Error: ${e.messagingErrorCode} - ${e.message}")
            throw AppException.BadRequest("Failed to send notification to topic: ${e.message}")
        }
    }

    override suspend fun sendNotificationToAllUsers(title: String, body: String, imageUrl: String?) {
        val tokens = userDeviceInfoRepository.getAllFirebaseTokens()
        if (tokens.isEmpty()) {
            println("No valid firebase tokens found to send notification.")
            return
        }
        tokens.forEach { token ->
            sendNotificationToToken(token, title, body, imageUrl)
        }
    }
}