package ir.speaking.feature.admin.notification.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminNotificationSendRequest(
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val targetType: String? = "topic", // "topic" or "token"
    val targetValue: String? = "aispeaking_plus_v1",
    val screen: String? = null, // "chat", "subscription", "profile", "stages", "main", "login"
    val stageId: String? = null, // e.g. "stage-01-inflight-london"
    val url: String? = null
)

@Serializable
data class AdminNotificationSendResponse(
    val success: Boolean,
    val messageId: String,
    val target: String,
    val targetType: String
)

@Serializable
data class RegisterTokenRequest(
    val token: String,
    val topic: String? = "aispeaking_plus_v1"
)

@Serializable
data class RegisterTokenResponse(
    val success: Boolean,
    val message: String
)
