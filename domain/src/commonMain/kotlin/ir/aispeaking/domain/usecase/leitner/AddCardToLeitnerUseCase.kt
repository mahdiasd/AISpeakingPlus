package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import ir.aispeaking.domain.validation.LeitnerWordValidator
import org.koin.core.annotation.Factory

@Factory
class AddCardToLeitnerUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(
        lexiconId: String?,
        customWord: String? = null,
        customMeaning: String? = null,
        customExample: String? = null,
        customAudioUrl: String? = null
    ): DataResult<Flashcard> {
        val cleanCustomWord = customWord?.trim()
        if (!cleanCustomWord.isNullOrBlank()) {
            val validation = LeitnerWordValidator.validate(cleanCustomWord)
            if (!validation.isValid) {
                return DataResult.Failure(
                    NetworkError.BadRequest(message = validation.errorMessage ?: "کلمه واردشده معتبر نیست.")
                )
            }
        } else if (lexiconId.isNullOrBlank()) {
            return DataResult.Failure(NetworkError.BadRequest(message = "کلمه واردشده معتبر نیست."))
        }

        return repository.addCard(
            lexiconId = lexiconId?.takeIf { it.isNotBlank() },
            customWord = cleanCustomWord,
            customMeaning = customMeaning,
            customExample = customExample,
            customAudioUrl = customAudioUrl
        )
    }

    suspend operator fun invoke(entry: DictionaryEntry): DataResult<Flashcard> {
        if (entry.id.isBlank() || entry.contextMeaningFa.isBlank()) {
            return DataResult.Failure(NetworkError.BadRequest(message = "امکان افزودن این کلمه به لایتنر وجود ندارد."))
        }
        val validation = LeitnerWordValidator.validate(entry.headword)
        if (!validation.isValid) {
            return DataResult.Failure(
                NetworkError.BadRequest(message = validation.errorMessage ?: "امکان افزودن این کلمه به لایتنر وجود ندارد.")
            )
        }
        return repository.addCard(
            lexiconId = entry.id.takeIf { it.isNotBlank() },
            customWord = entry.headword,
            customMeaning = entry.contextMeaningFa,
            customExample = entry.sampleSentenceEn,
            customAudioUrl = entry.audioUrl
        )
    }
}
