package ir.aispeaking.data.mapper.announcement

import ir.aispeaking.domain.model.Announcement
import ir.aispeaking.network.model.announcement.dto.AnnouncementDto

fun AnnouncementDto.toDomain(): Announcement {
    return Announcement(
        id = id,
        title = title,
        message = message,
        type = type,
        imageUrl = imageUrl,
        isBlocking = isBlocking,
        isActive = isActive,
        primaryButtonText = primaryButtonText,
        primaryButtonAction = primaryButtonAction,
        primaryButtonValue = primaryButtonValue,
        secondaryButtonText = secondaryButtonText,
        secondaryButtonAction = secondaryButtonAction,
        secondaryButtonValue = secondaryButtonValue,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
