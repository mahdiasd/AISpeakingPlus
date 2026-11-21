package ir.aispeaking.data.model.leitner

import kotlinx.serialization.Serializable

@Serializable
data class AddFlashcardRequestDto(
    val lexiconId: String? = null,
    val customWord: String? = null,
    val customMeaning: String? = null,
    val customExample: String? = null,
    val customAudioUrl: String? = null
)

@Serializable
data class CardReviewRequestDto(
    val cardId: String,
    val remembered: Boolean
)

@Serializable
data class LeitnerReminderRequestDto(
    val reminderTime: String
)

@Serializable
data class FlashcardDto(
    val id: String = "",
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
    val headword: String = "",
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val meaningFa: String = "",
    val exampleEn: String? = null,
    val exampleFa: String? = null,
    val audioUrl: String? = null
)

@Serializable
data class FlashcardListDto(
    val cards: List<FlashcardDto> = emptyList()
)

@Serializable
data class BoxCountsDto(
    val counts: Map<String, Long> = emptyMap()
)
