package ir.aispeaking.network.dto.challenge

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeDetailResponse(
    val challenge: ChallengeResponse,
    val userHaveSubscription: Boolean,
    val progress: ChallengeProgressResponse?
)
