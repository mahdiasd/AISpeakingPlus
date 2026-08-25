package ir.speaking.feature.scenario.scenario.dto.response

import ir.speaking.feature.scenario.progress.dto.response.ScenarioProgressResponse
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioDetailResponse(
    val scenario: ScenarioResponse,
    val userHaveSubscription: Boolean,
    val progress: ScenarioProgressResponse?
)
