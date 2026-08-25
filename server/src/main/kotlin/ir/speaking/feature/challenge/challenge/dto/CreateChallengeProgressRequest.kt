package ir.speaking.feature.challenge.challenge.dto

import ir.speaking.core.utils.now
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.challenge.progress.model.ChallengeProgress
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateChallengeProgressRequest(
    val challengeId: String,
    val score: Int
) {
    fun toProgress(userId: UUID) = ChallengeProgress(
        uid = UUID.randomUUID(),
        userId = userId,
        challengeId = challengeId.toUUID(),
        score = score,
        completedAt = LocalDateTime.now()
    )
}
