package ir.aispeaking.domain.usecase.translation

import ir.aispeaking.domain.repository.translation.TranslationRepository
import org.koin.core.annotation.Single

@Single
class TranslateUseCase(private val repo: TranslationRepository) {
    suspend operator fun invoke(text: String) = repo.translate(text)
}