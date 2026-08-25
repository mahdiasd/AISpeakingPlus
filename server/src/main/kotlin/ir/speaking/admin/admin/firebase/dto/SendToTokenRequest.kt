// File: src/main/kotlin/ir/speaking/admin/notification/dto/NotificationRequest.kt

package ir.speaking.admin.admin.firebase.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendToTokenRequest(
    val token: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null
)

@Serializable
data class SendToTopicRequest(
    val topic: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null
)