package ir.aispeaking.domain.model.dictionary

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryEntry(
    val id: String = "",
    val normalizedText: String,
    val headword: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val contextMeaningFa: String,
    val definitionEn: String? = null,
    val otherMeanings: List<String> = emptyList(),
    val sampleSentenceEn: String? = null,
    val sampleSentenceFa: String? = null,
    val audioUrl: String? = null,
    val createdAt: String? = null
)
