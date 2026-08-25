package ir.aispeaking.network.dto.challenge

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeProgressResponse(
    val id: String,
    val userId: String,
    val challengeId: String,
    val score: Int,
    val completedAt: String
)