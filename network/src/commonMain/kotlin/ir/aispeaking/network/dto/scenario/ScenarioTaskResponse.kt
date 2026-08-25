package ir.aispeaking.network.dto.scenario

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioTaskResponse(
    val id: String,
    val scenarioId: String,
    val description: String,
    val persianDescription: String,
    val createdAt: String
)
