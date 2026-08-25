package ir.aispeaking.network.dto.config

import kotlinx.serialization.Serializable

@Serializable
data class WelcomeMessageResponse(
    val id: String,
    val message: String,
    val title: String,
    val imageUrl: String,
    val isReadByUser: Boolean = false,
)