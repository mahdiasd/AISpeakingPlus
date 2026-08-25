package ir.aispeaking.domain.model.challenge

import kotlin.time.Instant

data class Challenge(
    val uid: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val aiName: String,
    val aiAvatar: String?,
    val points: Int,
    val createdAt: Instant?,
    val tasks: List<ChallengeTask>
)
