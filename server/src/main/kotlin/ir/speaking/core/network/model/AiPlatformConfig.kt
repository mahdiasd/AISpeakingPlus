package ir.speaking.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AiPlatformConfig(
    val baseUrl: String,
    val apiKey: String,
    val primaryModel: String,
    val fallbackModel: String
)