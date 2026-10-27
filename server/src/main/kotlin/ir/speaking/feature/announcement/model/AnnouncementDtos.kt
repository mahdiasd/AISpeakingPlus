package ir.speaking.feature.announcement.model

import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // INFO, WARNING, ERROR, SUCCESS
    val imageUrl: String? = null,
    val isBlocking: Boolean = false,
    val isActive: Boolean = false,
    val primaryButtonText: String = "فهمیدم",
    val primaryButtonAction: String = "CLOSE", // CLOSE, OPEN_URL, NAVIGATE, RELOAD
    val primaryButtonValue: String? = null,
    val secondaryButtonText: String? = null,
    val secondaryButtonAction: String? = null,
    val secondaryButtonValue: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class AnnouncementUpsertRequest(
    val title: String,
    val message: String,
    val type: String = "INFO", // INFO, WARNING, ERROR, SUCCESS
    val imageUrl: String? = null,
    val isBlocking: Boolean = false,
    val isActive: Boolean = false,
    val primaryButtonText: String = "فهمیدم",
    val primaryButtonAction: String = "CLOSE",
    val primaryButtonValue: String? = null,
    val secondaryButtonText: String? = null,
    val secondaryButtonAction: String? = null,
    val secondaryButtonValue: String? = null
)

@Serializable
data class AnnouncementListResponse(
    val announcements: List<AnnouncementDto>,
    val activeAnnouncementId: String? = null
)
