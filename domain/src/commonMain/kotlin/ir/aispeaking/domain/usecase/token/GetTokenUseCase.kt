package ir.aispeaking.domain.usecase.token

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetTokenUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(): Flow<DataResult<String>> = repo.getToken()
}