package ir.aispeaking.domain.usecase.user.auth

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class SendOtpUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(mobile: String): Flow<DataResult<String>> {
        return userRepository.sendOtp(mobile = mobile)
    }
}