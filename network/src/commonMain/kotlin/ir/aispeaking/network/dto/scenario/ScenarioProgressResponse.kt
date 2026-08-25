package ir.aispeaking.network.dto.scenario

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioProgressResponse(
    val id: String,
    val userId: String,
    val scenarioId: String,
    val score: Int,
    val completedAt: String
)