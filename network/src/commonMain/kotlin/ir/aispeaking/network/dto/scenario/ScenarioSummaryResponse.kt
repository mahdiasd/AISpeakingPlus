package ir.aispeaking.network.dto.scenario

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioSummaryResponse(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val aiAvatar: String? = null,
    val score: Int? = null,
)
