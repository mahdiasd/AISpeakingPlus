package ir.speaking.feature.leitner.model

import kotlinx.serialization.Serializable

@Serializable
data class UserFlashcard(
    val id: String,
    val userId: String,
    val lexiconId: String? = null,
    val customWord: String? = null,
    val customMeaning: String? = null,
    val customExample: String? = null,
    val customAudioUrl: String? = null,
    val boxNumber: Int,
    val reviewCount: Int,
    val lastReviewedAt: String? = null,
    val nextReviewAt: String,
    val createdAt: String,
    // Resolved presentation fields from linked lexicon:
    val headword: String = "",
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val meaningFa: String = "",
    val exampleEn: String? = null,
    val exampleFa: String? = null,
    val audioUrl: String? = null
)
