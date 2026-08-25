package ir.speaking.feature.scenario.progress.dto.request

import ir.speaking.core.utils.toUUID
import ir.speaking.feature.scenario.progress.model.ScenarioProgress
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class CreateScenarioProgressRequest(
    val scenarioId: String,
    val score: Int
) {
    fun toProgress(userId: UUID) = ScenarioProgress(
        id = UUID.randomUUID(),
        userId = userId,
        scenarioId = scenarioId.toUUID(),
        score = score,
        completedAt = LocalDateTime.now().toKotlinLocalDateTime()
    )
}
