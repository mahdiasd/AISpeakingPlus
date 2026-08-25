package ir.aispeaking.network.dto.translate

import kotlinx.serialization.Serializable

@Serializable
data class UpsertTranslationRequest(
    val uid: String? = null,
    val sourceText: String,
    val translatedText: String,
    val alternatives: List<AlternativeTranslationRequest>?,
    val example: String?
)

@Serializable
data class AlternativeTranslationRequest(
    val type: String,
    val translations: List<String>
)