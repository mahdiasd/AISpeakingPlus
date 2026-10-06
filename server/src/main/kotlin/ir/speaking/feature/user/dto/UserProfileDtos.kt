package ir.speaking.feature.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionSummaryDto(
    val isSubscriber: Boolean = false,
    val planType: String? = null,
    val planTitleFa: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val remainingDays: Int = 0
)

@Serializable
data class DetailedUserProfileResponse(
    val id: String,
    val phoneNumber: String,
    val nickName: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val avatar: String = "default_avatar",
    val score: Int = 0,
    val totalStars: Int = 0,
    val completedStagesCount: Int = 0,
    val languageLevel: String = "",
    val subscription: SubscriptionSummaryDto = SubscriptionSummaryDto()
)

@Serializable
data class UpdateProfileRequest(
    val nickName: String? = null,
    val avatar: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
