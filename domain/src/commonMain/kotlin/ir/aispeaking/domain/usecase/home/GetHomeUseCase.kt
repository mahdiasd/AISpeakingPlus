package ir.aispeaking.domain.usecase.home

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.home.Home
import ir.aispeaking.domain.repository.home.HomeRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetHomeUseCase(private val repo: HomeRepository) {
    suspend operator fun invoke(): Flow<DataResult<List<Home>>> {
        return repo.getHome()
    }
}