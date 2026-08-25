package ir.speaking.feature.scenario.progress.dto.response

import ir.speaking.feature.scenario.progress.model.ScenarioProgress
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioProgressResponse(
    val id: String,
    val userId: String,
    val scenarioId: String,
    val score: Int,
    val completedAt: String
)

fun ScenarioProgress.toResponse() = ScenarioProgressResponse(
    id = id.toString(),
    userId = userId.toString(),
    scenarioId = scenarioId.toString(),
    score = score,
    completedAt = completedAt.toString()
)