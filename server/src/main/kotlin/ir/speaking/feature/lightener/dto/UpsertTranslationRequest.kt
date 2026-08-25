package ir.speaking.feature.lightener.dto

import ir.speaking.feature.lightener.model.AlternativeTranslation
import kotlinx.serialization.Serializable

@Serializable
data class UpsertTranslationRequest(
    val uid: String? = null,
    val sourceText: String,
    val translatedText: String,
    val alternatives: List<AlternativeTranslation>?,
    val example: String?
)