package ir.aispeaking.data.model.dictionary

import kotlinx.serialization.Serializable

@Serializable
internal data class DictionaryLookupRequestDto(
    val word: String,
    val context: String? = null
)

@Serializable
internal data class DictionaryEntryDto(
    val id: String = "",
    val normalizedText: String = "",
    val headword: String = "",
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val contextMeaningFa: String = "",
    val otherMeanings: List<String> = emptyList(),
    val otherMeaningsJson: String? = null,
    val definitionEn: String? = null,
    val sampleSentenceEn: String? = null,
    val sampleSentenceFa: String? = null,
    val audioUrl: String? = null,
    val createdAt: String? = null
)
