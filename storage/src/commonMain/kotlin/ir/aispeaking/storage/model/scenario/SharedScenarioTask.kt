package ir.aispeaking.storage.model.scenario

import kotlinx.serialization.Serializable

@Serializable
data class SharedScenarioTask(
    val id: String,
    val scenarioId: String,
    val description: String,
    val persianDescription: String,
    val createdAt: String?
)