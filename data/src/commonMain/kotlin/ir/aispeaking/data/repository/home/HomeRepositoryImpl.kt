package ir.aispeaking.data.repository.home

import ir.aispeaking.data.mapper.home.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.home.Home
import ir.aispeaking.domain.repository.home.HomeRepository
import ir.aispeaking.network.api.home.HomeApiService

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class HomeRepositoryImpl(
    private val apiService: HomeApiService,
) : HomeRepository {

    override suspend fun getHome(): Flow<DataResult<List<Home>>> = flow {
        when (val result =
            safeCall { apiService.getHome() }) {
            is DataResult.Success -> {
                emit(DataResult.Success(result.data.map { it.toDomain() }))
            }

            is DataResult.Failure -> {
                emit(DataResult.Failure(result.appError))
            }
        }
    }
}