package ir.speaking.feature.category.repository

import ir.speaking.feature.category.model.Category
import java.util.*

interface CategoryRepository {
    suspend fun getCategoryById(id: UUID): Category?
    suspend fun getAllCategories(): List<Category>
    suspend fun createCategory(category: Category): Category
    suspend fun updateCategory(category: Category): Category?
    suspend fun deleteCategory(id: UUID): Boolean
}
