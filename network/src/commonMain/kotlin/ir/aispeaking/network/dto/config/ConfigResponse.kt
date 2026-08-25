package ir.aispeaking.network.dto.config

import kotlinx.serialization.Serializable

@Serializable
data class ConfigResponse(
    val update: UpdateResponse,
    val welcomeMessage: WelcomeMessageResponse?,
    val tokenAlive: Boolean,
)