package ir.aispeaking.domain.model.scenario

import kotlin.time.Instant

data class ScenarioTask(
    val id: String,
    val scenarioId: String,
    val description: String,
    val persianDescription: String,
    val createdAt: Instant?,

    val finished : Boolean = false
)