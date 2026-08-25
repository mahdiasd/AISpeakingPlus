package ir.aispeaking.domain.usecase.translation

import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.domain.repository.translation.TranslationRepository
import org.koin.core.annotation.Single

@Single
class UpdateTranslateUseCase(private val repo: TranslationRepository) {
    suspend operator fun invoke(translation: Translation) =
        repo.updateTranslate(translation = translation)
}