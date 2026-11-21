package ir.aispeaking.domain.model.leitner

import kotlinx.serialization.Serializable

@Serializable
data class Flashcard(
    val id: String,
    val userId: String = "",
    val lexiconId: String? = null,
    val customWord: String? = null,
    val customMeaning: String? = null,
    val customExample: String? = null,
    val customAudioUrl: String? = null,
    val boxNumber: Int = 1,
    val reviewCount: Int = 0,
    val lastReviewedAt: String? = null,
    val nextReviewAt: String = "",
    val createdAt: String = "",
    // Resolved presentation fields
    val headword: String = "",
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val meaningFa: String = "",
    val exampleEn: String? = null,
    val exampleFa: String? = null,
    val audioUrl: String? = null
)
