package ir.speaking.feature.admin.notification.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import ir.speaking.feature.admin.notification.model.AdminNotificationSendRequest
import ir.speaking.feature.admin.notification.model.AdminNotificationSendResponse
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

class AdminNotificationService {
    private val logger = LoggerFactory.getLogger(AdminNotificationService::class.java)

    init {
        ensureFirebaseInitialized()
    }

    @Synchronized
    private fun ensureFirebaseInitialized() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                val serviceAccountStream: InputStream? =
                    this::class.java.classLoader.getResourceAsStream("service-account-key.json")
                        ?: File("server/src/main/resources/service-account-key.json").takeIf { it.exists() }?.inputStream()
                        ?: File("src/main/resources/service-account-key.json").takeIf { it.exists() }?.inputStream()
                        ?: File("service-account-key.json").takeIf { it.exists() }?.inputStream()

                if (serviceAccountStream != null) {
                    val credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    val options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId("aispeaking-132a3")
                        .build()
                    FirebaseApp.initializeApp(options)
                    logger.info("FirebaseApp initialized successfully with service-account-key.json")
                } else {
                    logger.warn("service-account-key.json not found on classpath or disk")
                }
            } catch (e: Exception) {
                logger.error("Failed to initialize FirebaseApp", e)
            }
        }
    }

    fun sendNotification(req: AdminNotificationSendRequest): AdminNotificationSendResponse {
        ensureFirebaseInitialized()
        if (FirebaseApp.getApps().isEmpty()) {
            throw IllegalStateException("فایربیس در سرور مقداردهی نشده است؛ فایل service-account-key.json یافت نشد")
        }

        val notificationBuilder = Notification.builder()
            .setTitle(req.title)
            .setBody(req.body)

        if (!req.imageUrl.isNullOrBlank()) {
            notificationBuilder.setImage(req.imageUrl.trim())
        }
        val notification = notificationBuilder.build()

        // Build data map for deep linking
        val dataMap = mutableMapOf<String, String>()
        dataMap["title"] = req.title
        dataMap["body"] = req.body
        if (!req.imageUrl.isNullOrBlank()) {
            dataMap["image"] = req.imageUrl.trim()
            dataMap["icon"] = req.imageUrl.trim()
        }

        val isNoLink = req.screen.isNullOrBlank() || req.screen.trim().lowercase() == "none"
        if (!isNoLink) {
            if (!req.stageId.isNullOrBlank()) {
                dataMap["stage_id"] = req.stageId.trim()
                dataMap["stageId"] = req.stageId.trim()
            }
            if (!req.screen.isNullOrBlank()) {
                dataMap["screen"] = req.screen.trim().lowercase()
            }
            if (!req.url.isNullOrBlank()) {
                dataMap["url"] = req.url.trim()
            }
            dataMap["click_action"] = "ir.aispeaking.NOTIFICATION_CLICK"
        }

        // Web Link resolution
        val webLink = when {
            isNoLink -> "/"
            !req.url.isNullOrBlank() -> req.url.trim()
            !req.stageId.isNullOrBlank() -> "/#chat?stageId=${req.stageId.trim()}"
            req.screen?.lowercase() == "chat" && !req.stageId.isNullOrBlank() -> "/#chat?stageId=${req.stageId.trim()}"
            req.screen?.lowercase() == "subscription" -> "/#subscription"
            req.screen?.lowercase() == "profile" -> "/#profile"
            req.screen?.lowercase() == "stages" -> "/#stages"
            else -> "/"
        }

        val messageBuilder = Message.builder()
            .setNotification(notification)
            .putAllData(dataMap)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(
                        AndroidNotification.builder()
                            .setChannelId("aispeaking_general_channel")
                            .setDefaultSound(true)
                            .setDefaultVibrateTimings(true)
                            .build()
                    )
                    .build()
            )
            .setWebpushConfig(
                WebpushConfig.builder()
                    .putHeader("Urgency", "high")
                    .setFcmOptions(
                        WebpushFcmOptions.builder()
                            .setLink(webLink)
                            .build()
                    )
                    .build()
            )

        val targetType = req.targetType?.lowercase()?.trim() ?: "topic"
        val targetValue = req.targetValue?.trim()?.ifBlank { null }

        when (targetType) {
            "token" -> {
                if (targetValue.isNullOrBlank()) {
                    throw IllegalArgumentException("توکن دستگاه (Device Token) الزامی است")
                }
                messageBuilder.setToken(targetValue)
            }
            "topic" -> {
                val topic = targetValue ?: "aispeaking_plus_v1"
                messageBuilder.setTopic(topic)
            }
            else -> {
                val topic = targetValue ?: "aispeaking_plus_v1"
                messageBuilder.setTopic(topic)
            }
        }

        val message = messageBuilder.build()
        val messageId = FirebaseMessaging.getInstance().send(message)
        logger.info("FCM Notification sent successfully: messageId=$messageId, targetType=$targetType, targetValue=$targetValue")

        return AdminNotificationSendResponse(
            success = true,
            messageId = messageId,
            target = targetValue ?: "aispeaking_plus_v1",
            targetType = targetType
        )
    }

    fun subscribeTokenToTopic(token: String, topic: String = "aispeaking_plus_v1"): TopicManagementResponse {
        ensureFirebaseInitialized()
        if (FirebaseApp.getApps().isEmpty()) {
            throw IllegalStateException("فایربیس در سرور مقداردهی نشده است")
        }
        return FirebaseMessaging.getInstance().subscribeToTopic(listOf(token), topic)
    }
}
