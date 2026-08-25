package ir.aispeaking.network.dto.scenario

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioDetailResponse(
    val scenario: ScenarioResponse,
    val userHaveSubscription: Boolean,
    val progress: ScenarioProgressResponse?
)

