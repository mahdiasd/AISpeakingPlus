package ir.aispeaking.network.dto.translate

import kotlinx.serialization.Serializable

@Serializable
data class AlternativeTranslationResponse(
    val type: String,
    val translations: List<String>
)