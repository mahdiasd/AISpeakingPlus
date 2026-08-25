package ir.speaking.feature.scenario.progress.dto.request

import ir.speaking.core.utils.toUUID
import ir.speaking.feature.scenario.progress.model.ScenarioProgress
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class UpdateProgressRequest(
    val id: String,
    val score: Int,
    val scenarioId: String
) {
    fun toProgress(userId: UUID) = ScenarioProgress(
        id = id.toUUID(),
        score = score,
        completedAt = LocalDateTime.now().toKotlinLocalDateTime(),
        userId = userId,
        scenarioId = scenarioId.toUUID()
    )
}