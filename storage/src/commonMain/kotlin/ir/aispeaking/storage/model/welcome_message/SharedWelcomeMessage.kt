package ir.aispeaking.storage.model.welcome_message

import kotlinx.serialization.Serializable

@Serializable
data class SharedWelcomeMessage(
    val id: String,
    val message: String,
    val title: String,
    val imageUrl: String,
    val isReadByUser: Boolean = false,
)