package ir.speaking.feature.scenario.progress.model

import kotlinx.datetime.LocalDateTime
import java.util.*

data class ScenarioProgress(
    val id: UUID,
    val userId: UUID,
    val scenarioId: UUID,
    val score: Int,
    val completedAt: LocalDateTime
)
