package ir.aispeaking.domain.repository.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.leitner.Flashcard

interface LeitnerRepository {
    suspend fun addCard(
        lexiconId: String?,
        customWord: String? = null,
        customMeaning: String? = null,
        customExample: String? = null,
        customAudioUrl: String? = null
    ): DataResult<Flashcard>

    suspend fun getDueCards(limit: Int = 20): DataResult<List<Flashcard>>

    suspend fun getCardsByBox(boxNumber: Int): DataResult<List<Flashcard>>

    suspend fun getAllCards(): DataResult<List<Flashcard>>

    suspend fun submitCardReview(cardId: String, remembered: Boolean): DataResult<Flashcard>

    suspend fun deleteCard(cardId: String): DataResult<Boolean>

    suspend fun getBoxCounts(): DataResult<Map<Int, Long>>

    suspend fun getReminderTime(): DataResult<String?>

    suspend fun setReminderTime(time: String): DataResult<Boolean>
}
