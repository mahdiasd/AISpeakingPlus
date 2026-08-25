package ir.speaking.admin.scenario.scenario.dto

import ir.speaking.feature.scenario.task.model.ScenarioTask
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class SaveScenarioTaskRequest(
    val id: String? = null,
    val description: String,
    val persianDescription: String
)

fun SaveScenarioTaskRequest.toScenarioTask(scenarioId: UUID) = ScenarioTask(
    id = if (id.isNullOrEmpty()) UUID.randomUUID() else UUID.fromString(id),
    scenarioId = scenarioId,
    description = description,
    persianDescription = persianDescription,
    createdAt = LocalDateTime.now().toKotlinLocalDateTime()
)