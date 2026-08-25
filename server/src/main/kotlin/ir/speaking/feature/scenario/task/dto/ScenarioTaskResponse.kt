package ir.speaking.feature.scenario.task.dto

import ir.speaking.feature.scenario.task.model.ScenarioTask
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioTaskResponse(
    val id: String,
    val scenarioId: String,
    val description: String,
    val persianDescription: String,
    val createdAt: String
)

fun ScenarioTask.toResponse() = ScenarioTaskResponse(
    id = id.toString(),
    scenarioId = scenarioId.toString(),
    description = description,
    persianDescription = persianDescription,
    createdAt = createdAt.toString()
)