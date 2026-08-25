package ir.aispeaking.data.mapper.scenario

import ir.aispeaking.domain.model.scenario.ScenarioTask
import ir.aispeaking.network.dto.scenario.ScenarioTaskResponse
import ir.aispeaking.utils.time.toInstantOrNull

fun ScenarioTask.toResponse() = ScenarioTaskResponse(
    id = id,
    scenarioId = scenarioId,
    description = description,
    persianDescription = persianDescription,
    createdAt = createdAt.toString()
)

fun ScenarioTaskResponse.toDomain() = ScenarioTask(
    id = id,
    scenarioId = scenarioId,
    persianDescription = persianDescription,
    description = description,
    createdAt = createdAt.toInstantOrNull()
)