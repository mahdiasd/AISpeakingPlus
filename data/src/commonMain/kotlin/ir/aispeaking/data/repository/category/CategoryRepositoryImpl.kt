package ir.aispeaking.data.repository.category

import ir.aispeaking.data.mapper.category.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.category.CategoryRepository
import ir.aispeaking.network.api.category.CategoryApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class CategoryRepositoryImpl(
    private val apiService: CategoryApiService,
) : CategoryRepository {


    override suspend fun getCategories(): Flow<DataResult<List<Category>>> = flow {
        when (val result = safeCall { apiService.getCategories() }) {
            is DataResult.Success -> emit(DataResult.Success(result.data.map { it.toDomain() }))
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }
}