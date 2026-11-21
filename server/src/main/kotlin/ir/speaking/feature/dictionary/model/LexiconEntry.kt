package ir.speaking.feature.dictionary.model

import kotlinx.serialization.Serializable

@Serializable
data class LexiconEntry(
    val id: String,
    val normalizedText: String,
    val headword: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val contextMeaningFa: String,
    val otherMeaningsJson: String? = null,
    val definitionEn: String? = null,
    val sampleSentenceEn: String? = null,
    val sampleSentenceFa: String? = null,
    val audioUrl: String? = null,
    val createdAt: String? = null
)
