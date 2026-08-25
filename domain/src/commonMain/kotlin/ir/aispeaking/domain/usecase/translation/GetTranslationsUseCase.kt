package ir.aispeaking.domain.usecase.translation

import ir.aispeaking.domain.repository.translation.TranslationRepository
import org.koin.core.annotation.Single

@Single
class GetTranslationsUseCase(private val repo: TranslationRepository) {
    suspend operator fun invoke(
        searchText: String?,
        page: Int,
        pageSize: Int? = null
    ) =
        repo.getTranslations(
            searchText = searchText,
            page = page,
            pageSize = pageSize
        )
}