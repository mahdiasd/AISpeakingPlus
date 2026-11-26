package ir.speaking.feature.dictionary.dto

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryLookupRequest(
    val word: String,
    val contextSentence: String? = null
)

@Serializable
data class DictionaryLookupResponse(
    val id: String,
    val headword: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val contextMeaningFa: String,
    val otherMeaningsFa: List<String> = emptyList(),
    val definitionEn: String? = null,
    val sampleSentenceEn: String? = null,
    val sampleSentenceFa: String? = null,
    val audioUrl: String? = null
)

@Serializable
data class AiDictionaryResult(
    val isValid: Boolean = true,
    val rejectionReasonFa: String? = null,
    val headword: String = "",
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val contextMeaningFa: String = "",
    val definitionEn: String? = null,
    val otherMeaningsFa: List<String> = emptyList(),
    val sampleSentenceEn: String? = null,
    val sampleSentenceFa: String? = null
)
