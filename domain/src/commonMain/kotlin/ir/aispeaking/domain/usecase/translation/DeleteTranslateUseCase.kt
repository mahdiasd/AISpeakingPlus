package ir.aispeaking.domain.usecase.translation

import ir.aispeaking.domain.repository.translation.TranslationRepository
import org.koin.core.annotation.Single

@Single
class DeleteTranslateUseCase(private val repo: TranslationRepository) {
    suspend operator fun invoke(uid: String) =
        repo.delete(uid)
}