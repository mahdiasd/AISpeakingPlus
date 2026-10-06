package ir.aispeaking.domain.model.user

data class SubscriptionSummary(
    val isSubscriber: Boolean = false,
    val planType: String? = null,
    val planTitleFa: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val remainingDays: Int = 0
) {
    val isExpiringSoon: Boolean
        get() = isSubscriber && remainingDays in 1..7
}

data class UserProfile(
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
    val subscription: SubscriptionSummary = SubscriptionSummary(),
    val isGuest: Boolean = false
) {
    val displayName: String
        get() = when {
            isGuest -> "زبان‌آموز مهمان"
            nickName.isNotBlank() -> nickName
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            else -> "کاربر"
        }
}

data class UpdateProfileInput(
    val nickName: String? = null,
    val avatar: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
