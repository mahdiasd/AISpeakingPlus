package ir.aispeaking.network.api.category

import ir.aispeaking.network.dto.category.CategoryResponse
import ir.aispeaking.network.model.NetworkResponse


interface CategoryApiService {
    suspend fun getCategories(): NetworkResponse<List<CategoryResponse>>
}