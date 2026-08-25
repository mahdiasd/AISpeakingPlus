package ir.aispeaking.domain.usecase.user.auth

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class VerifyOtpUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(mobile: String, code: String): Flow<DataResult<User>> {
        return userRepository.verifyOtp(mobile = mobile, otpCode = code)
    }
}