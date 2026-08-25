package ir.speaking.feature.challenge.task.model

import kotlinx.datetime.LocalDateTime
import java.util.*

data class ChallengeTask(
    val id: UUID,
    val challengeId: UUID,
    val description: String,
    val persianDescription: String,
    val createdAt: LocalDateTime
)