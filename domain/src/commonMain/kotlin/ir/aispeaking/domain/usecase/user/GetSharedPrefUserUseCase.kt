package ir.aispeaking.domain.usecase.user

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetSharedPrefUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Flow<DataResult<User>> {
        return userRepository.getSharedPreferencesUser()
    }
}