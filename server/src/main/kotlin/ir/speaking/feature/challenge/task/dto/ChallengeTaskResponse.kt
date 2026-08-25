package ir.speaking.feature.challenge.task.dto

import ir.speaking.core.utils.now
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.challenge.task.model.ChallengeTask
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ChallengeTaskResponse(
    val id: String,
    val description: String,
    val persianDescription: String,
)

@Serializable
data class ChallengeTaskRequest(
    val challengeId: String,
    val description: String,
    val persianDescription: String,
)


fun ChallengeTaskRequest.toModel() = ChallengeTask(
    challengeId = challengeId.toUUID(),
    description = description,
    persianDescription = persianDescription,
    id = UUID.randomUUID(),
    createdAt = LocalDateTime.now(),
)
