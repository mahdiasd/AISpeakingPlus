package ir.aispeaking.network.dto.translate

import kotlinx.serialization.Serializable

@Serializable
data class TranslationResponse(
    val uid: String,
    val userId: String,

    val sourceText: String,
    val translatedText: String,
    val alternatives: List<AlternativeTranslationResponse>?,
    val example: String?
)