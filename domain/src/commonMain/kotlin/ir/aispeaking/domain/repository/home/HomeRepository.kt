package ir.aispeaking.domain.repository.home

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.home.Home
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun getHome(): Flow<DataResult<List<Home>>>
}