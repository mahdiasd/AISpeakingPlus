package ir.speaking.feature.leitner.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReviewCardRequest(
    val cardId: String,
    val remembered: Boolean
)

@Serializable
data class AddFlashcardRequest(
    val lexiconId: String? = null,
    val customWord: String? = null,
    val customMeaning: String? = null,
    val customExample: String? = null,
    val customAudioUrl: String? = null
)

@Serializable
data class SetReminderTimeRequest(
    val reminderTime: String? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val enabled: Boolean? = true
)

@Serializable
data class ReminderTimeResponse(
    val reminderTime: String,
    val enabled: Boolean = true
)

@Serializable
data class LeitnerStatsResponse(
    val totalCards: Long,
    val dueCards: Long,
    val boxCounts: Map<Int, Long>
)
