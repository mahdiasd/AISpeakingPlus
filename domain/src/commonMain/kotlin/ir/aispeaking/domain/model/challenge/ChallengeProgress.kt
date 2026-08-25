package ir.aispeaking.domain.model.challenge

import kotlin.time.Instant

data class ChallengeProgress(
    val uid: String,
    val userId: String,
    val challengeId: String,
    val score: Int,
    val completedAt: Instant?
)
