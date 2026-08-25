package ir.aispeaking.network.dto.challenge

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeTaskResponse(
    val id: String,
    val description: String,
    val persianDescription: String,
)