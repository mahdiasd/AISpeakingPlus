package ir.aispeaking.domain.model.scenario

import kotlin.time.Instant

data class ScenarioProgress(
    val id: String,
    val userId: String,
    val scenarioId: String,
    val score: Int,
    val completedAt: Instant
)
