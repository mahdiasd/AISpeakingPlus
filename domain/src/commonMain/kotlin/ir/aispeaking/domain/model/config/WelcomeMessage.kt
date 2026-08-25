package ir.aispeaking.domain.model.config

data class WelcomeMessage(
    val id: String,
    val message: String,
    val title: String,
    val imageUrl: String,
    val isReadByUser: Boolean = false,
)