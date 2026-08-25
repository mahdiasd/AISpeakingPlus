package ir.speaking.feature.challenge.progress.model

import kotlinx.datetime.LocalDateTime
import java.util.*

data class ChallengeProgress(
    val uid: UUID,
    val userId: UUID,
    val challengeId: UUID,
    val score: Int,
    val completedAt: LocalDateTime
)
