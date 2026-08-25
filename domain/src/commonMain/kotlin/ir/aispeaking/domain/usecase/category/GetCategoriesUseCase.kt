package ir.aispeaking.domain.usecase.category

import ir.aispeaking.domain.repository.category.CategoryRepository
import org.koin.core.annotation.Single

@Single
class GetCategoriesUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke() = repo.getCategories()
}