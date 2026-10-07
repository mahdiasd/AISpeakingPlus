package ir.aispeaking.data.mapper.user

import ir.aispeaking.domain.model.user.SubscriptionSummary
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.network.model.user.dto.SubscriptionSummaryDto
import ir.aispeaking.network.model.user.dto.UpdateProfileRequestDto
import ir.aispeaking.network.model.user.dto.UserProfileResponseDto
import ir.aispeaking.storage.model.user.SharedPrefUser

fun SubscriptionSummaryDto.toDomain(): SubscriptionSummary {
    return SubscriptionSummary(
        isSubscriber = isSubscriber,
        planType = planType,
        planTitleFa = planTitleFa,
        startedAt = startedAt,
        expiresAt = expiresAt,
        remainingDays = remainingDays
    )
}

fun UserProfileResponseDto.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        phoneNumber = phoneNumber,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        avatar = avatar,
        score = score,
        totalStars = totalStars,
        completedStagesCount = completedStagesCount,
        languageLevel = languageLevel,
        subscription = subscription.toDomain(),
        isGuest = false
    )
}

fun UpdateProfileInput.toDto(): UpdateProfileRequestDto {
    return UpdateProfileRequestDto(
        nickName = nickName?.trim()?.takeIf { it.isNotBlank() },
        avatar = avatar?.trim()?.takeIf { it.isNotBlank() },
        firstName = firstName?.trim()?.takeIf { it.isNotBlank() },
        lastName = lastName?.trim()?.takeIf { it.isNotBlank() },
        languageLevel = languageLevel?.trim()?.takeIf { it.isNotBlank() }
    )
}

fun SharedPrefUser.toGuestDomainProfile(guestStars: Int = 0, guestScore: Int = 0): UserProfile {
    return UserProfile(
        id = uid.ifBlank { "guest" },
        phoneNumber = mobile,
        nickName = nickName.ifBlank { "زبان‌آموز مهمان" },
        firstName = firstName,
        lastName = lastName,
        avatar = avatar.ifBlank { "default_avatar" },
        score = if (score > 0) score else guestScore,
        totalStars = guestStars,
        completedStagesCount = if (guestStars > 0) 1 else 0,
        languageLevel = languageLevel,
        subscription = SubscriptionSummary(
            isSubscriber = false,
            planType = null,
            planTitleFa = "طرح رایگان",
            remainingDays = 0
        ),
        isGuest = true
    )
}
