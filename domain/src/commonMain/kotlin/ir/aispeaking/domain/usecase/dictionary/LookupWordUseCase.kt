package ir.aispeaking.domain.usecase.dictionary

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.repository.dictionary.DictionaryRepository
import ir.aispeaking.domain.validation.LeitnerWordValidator
import org.koin.core.annotation.Factory

@Factory
class LookupWordUseCase(
    private val repository: DictionaryRepository
) {
    suspend operator fun invoke(word: String, sentenceContext: String? = null): DataResult<DictionaryEntry> {
        val cleanWord = word.trim()
        val validation = LeitnerWordValidator.validate(cleanWord)
        if (!validation.isValid) {
            return DataResult.Failure(
                NetworkError.BadRequest(message = validation.errorMessage ?: "کلمه واردشده معتبر نیست.")
            )
        }
        return repository.lookupWord(cleanWord, sentenceContext?.trim())
    }
}
