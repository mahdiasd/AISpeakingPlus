package ir.aispeaking.network.dto.challenge

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeSummaryResponse(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val description: String,
    val aiAvatar: String?,
    val score: Int,
)
