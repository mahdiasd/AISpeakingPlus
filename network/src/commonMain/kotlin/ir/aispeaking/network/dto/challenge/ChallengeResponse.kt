package ir.aispeaking.network.dto.challenge

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeResponse(
    val uid: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val aiName: String,
    val aiAvatar: String?,
    val points: Int,
    val createdAt: String,
    val tasks: List<ChallengeTaskResponse>
)
