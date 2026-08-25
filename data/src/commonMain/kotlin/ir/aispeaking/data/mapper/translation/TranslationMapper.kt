package ir.aispeaking.data.mapper.translation

import ir.aispeaking.domain.model.translate.AlternativeTranslation
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.network.dto.translate.AlternativeTranslationRequest
import ir.aispeaking.network.dto.translate.AlternativeTranslationResponse
import ir.aispeaking.network.dto.translate.TranslationResponse
import ir.aispeaking.network.dto.translate.UpsertTranslationRequest
import ir.aispeaking.utils.immutableListOf
import kotlinx.collections.immutable.toImmutableList

fun TranslationResponse.toDomain(): Translation {
    return Translation(
        uid = uid,
        sourceText = sourceText,
        translatedText = translatedText,
        alternatives = alternatives?.map { it.toDomain() }?.toImmutableList() ?: immutableListOf(),
        example = example ?: ""
    )
}

fun AlternativeTranslationResponse.toDomain(): AlternativeTranslation {
    return AlternativeTranslation(type = type, translations = translations.toImmutableList())
}

fun Translation.toRequest(): UpsertTranslationRequest {
    return UpsertTranslationRequest(
        uid = uid,
        sourceText = sourceText,
        translatedText = translatedText,
        alternatives = alternatives.map { it.toRequest() },
        example = example
    )
}

fun AlternativeTranslation.toRequest(): AlternativeTranslationRequest {
    return AlternativeTranslationRequest(type = type, translations = translations)
}
