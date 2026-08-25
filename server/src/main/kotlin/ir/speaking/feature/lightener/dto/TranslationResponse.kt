package ir.speaking.feature.lightener.dto

import ir.speaking.feature.lightener.model.AlternativeTranslation
import ir.speaking.feature.lightener.model.Translation
import kotlinx.serialization.Serializable

@Serializable
data class TranslationResponse(
    val uid: String,
    val userId: String,

    val sourceText: String,
    val translatedText: String,
    val alternatives: List<AlternativeTranslation>?,
    val example: String?
)

fun Translation.toResponse(): TranslationResponse {
    return TranslationResponse(
        uid = uid.toString(),
        userId = userId.toString(),
        sourceText = sourceText,
        translatedText = translatedText,
        alternatives = alternatives,
        example = example
    )
}