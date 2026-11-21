package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
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
        return repository.addCard(
            lexiconId = lexiconId,
            customWord = customWord,
            customMeaning = customMeaning,
            customExample = customExample,
            customAudioUrl = customAudioUrl
        )
    }

    suspend operator fun invoke(entry: DictionaryEntry): DataResult<Flashcard> {
        return repository.addCard(
            lexiconId = entry.id.takeIf { it.isNotBlank() },
            customWord = entry.headword,
            customMeaning = entry.contextMeaningFa,
            customExample = entry.sampleSentenceEn,
            customAudioUrl = entry.audioUrl
        )
    }
}
