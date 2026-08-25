package ir.aispeaking.data.mapper.scenario

import ir.aispeaking.domain.model.scenario.ScenarioProgress
import ir.aispeaking.network.dto.scenario.ScenarioProgressResponse
import ir.aispeaking.utils.time.toInstant

fun ScenarioProgressResponse.toDomain(): ScenarioProgress {
    return ScenarioProgress(
        id = this.id,
        userId = this.userId,
        scenarioId = this.scenarioId,
        score = this.score,
        completedAt = this.completedAt.toInstant(),
    )
}