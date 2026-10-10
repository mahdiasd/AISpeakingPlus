package ir.speaking.feature.admin.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminLoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AdminInfoDto(
    val id: String,
    val username: String,
    val fullName: String,
    val role: String
)

@Serializable
data class AdminLoginResponse(
    val token: String,
    val admin: AdminInfoDto
)

@Serializable
data class AdminStageUpsertRequest(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val titleFa: String,
    val briefing: String,
    val briefingFa: String,
    val targetObjective: String,
    val targetObjectiveFa: String = "",
    val characterBehavior: String? = null,
    val backgroundUrl: String,
    val characterName: String,
    val characterAvatarUrl: String? = null,
    val characterGender: String = "Woman",
    val voiceId: String? = null,
    val initialSpeaker: String = "Model",
    val maxTurns: Int = 12,
    val status: String = "PUBLISHED",
    val shiftSubsequent: Boolean = false
)

@Serializable
data class AdminStageItemDto(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val titleFa: String,
    val briefing: String,
    val briefingFa: String,
    val targetObjective: String,
    val targetObjectiveFa: String,
    val characterBehavior: String? = null,
    val backgroundUrl: String,
    val characterName: String,
    val characterAvatarUrl: String?,
    val characterGender: String,
    val voiceId: String?,
    val initialSpeaker: String,
    val maxTurns: Int,
    val status: String,
    val createdAt: String
)

@Serializable
data class AdminUserItemDto(
    val id: String,
    val mobile: String,
    val nickName: String,
    val score: Int,
    val avatar: String,
    val status: String,
    val hasActiveSubscription: Boolean,
    val subscriptionExpiresAt: String? = null,
    val createdAt: String
)

@Serializable
data class AdminUserDetailDto(
    val id: String,
    val mobile: String,
    val nickName: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val score: Int,
    val avatar: String,
    val status: String,
    val suspendedReason: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val completedStagesCount: Int = 0,
    val activeSubscription: AdminSubscriptionItemDto? = null
)

@Serializable
data class AdminUserStatusUpdateRequest(
    val status: String,
    val reason: String? = null
)

@Serializable
data class AdminSubscriptionGrantRequest(
    val planType: String = "MONTHLY",
    val durationDays: Int = 30,
    val reason: String? = null
)

@Serializable
data class AdminSubscriptionItemDto(
    val id: String,
    val planType: String,
    val status: String,
    val grantSource: String,
    val grantedBy: String? = null,
    val grantedByAdminName: String? = null,
    val grantReason: String? = null,
    val startedAt: String,
    val expiresAt: String,
    val createdAt: String
)

@Serializable
data class AdminDashboardStatsDto(
    val totalUsers: Long,
    val activeUsers: Long,
    val activeSubscriptions: Long,
    val totalStages: Long,
    val publishedStages: Long
)

@Serializable
data class AdminAuditLogItemDto(
    val id: String,
    val adminId: String? = null,
    val adminName: String? = null,
    val action: String,
    val targetType: String,
    val targetId: String,
    val detailsJson: String,
    val createdAt: String
)

@Serializable
data class AdminStageReorderRequest(
    val newOrderIndex: Int,
    val shiftSubsequent: Boolean = true
)

@Serializable
data class AdminMediaUploadResponse(
    val url: String,
    val filename: String,
    val mimeType: String = "image/webp",
    val sizeBytes: Long = 0L
)

