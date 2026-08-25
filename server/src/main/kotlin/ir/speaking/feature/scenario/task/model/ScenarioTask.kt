package ir.speaking.feature.scenario.task.model

import kotlinx.datetime.LocalDateTime
import java.util.*


data class ScenarioTask(
    val id: UUID,
    val scenarioId: UUID,
    val description: String,
    val persianDescription: String,
    val createdAt: LocalDateTime
)