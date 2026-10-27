package ir.aispeaking.network.model.announcement.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val imageUrl: String? = null,
    val isBlocking: Boolean = false,
    val isActive: Boolean = false,
    val primaryButtonText: String = "فهمیدم",
    val primaryButtonAction: String = "CLOSE",
    val primaryButtonValue: String? = null,
    val secondaryButtonText: String? = null,
    val secondaryButtonAction: String? = null,
    val secondaryButtonValue: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)
