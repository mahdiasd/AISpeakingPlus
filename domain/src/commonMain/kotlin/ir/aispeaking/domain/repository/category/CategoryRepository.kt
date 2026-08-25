package ir.aispeaking.domain.repository.category

import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.data_result.DataResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    suspend fun getCategories(): Flow<DataResult<List<Category>>>
}