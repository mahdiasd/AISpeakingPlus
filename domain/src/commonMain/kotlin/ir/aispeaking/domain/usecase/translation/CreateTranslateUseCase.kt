package ir.aispeaking.domain.usecase.translation

import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.domain.repository.translation.TranslationRepository
import org.koin.core.annotation.Single

@Single
class CreateTranslateUseCase(private val repo: TranslationRepository) {
    suspend operator fun invoke(translation: Translation) =
        repo.createTranslate(translation = translation)
}