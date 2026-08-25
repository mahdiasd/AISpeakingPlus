package ir.speaking.feature.scenario.scenario.dto.response

import ir.speaking.feature.scenario.scenario.model.Scenario
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioSummaryResponse(
    val id: String,
    val title: String,
    val imageUrl: String?,
)

fun Scenario.toSummaryResponse(baseUrl: String) = ScenarioSummaryResponse(
    id = id.toString(),
    title = title,
    imageUrl = if (imageUrl != null) "$baseUrl/resources/$imageUrl" else null,
)