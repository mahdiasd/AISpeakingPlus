package ir.aispeaking.network.model.health.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponseDto(
    val status: String
)
