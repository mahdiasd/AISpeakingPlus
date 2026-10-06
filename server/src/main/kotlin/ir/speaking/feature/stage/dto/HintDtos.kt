package ir.speaking.feature.stage.dto

import kotlinx.serialization.Serializable

@Serializable
data class HintMessageRequest(
    val role: String,
    val content: String
)

@Serializable
data class HintRequest(
    val messages: List<HintMessageRequest> = emptyList()
)

@Serializable
data class HintResponse(
    val suggestionEn: String,
    val explanationFa: String
)
