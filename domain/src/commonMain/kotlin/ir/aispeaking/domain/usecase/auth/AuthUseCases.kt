package ir.aispeaking.domain.usecase.auth

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.repository.auth.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class GetCurrentAccessTierUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AccessTier {
        return authRepository.getCurrentAccessTier()
    }
}

@Factory
class GetAuthTokenUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String? {
        return authRepository.getToken()
    }
}

@Factory
class IsLoggedInUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
}

@Factory
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}

@Factory
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): DataResult<Unit> {
        return authRepository.logout()
    }
}

sealed interface AuthStatus {
    data class Authenticated(val user: User) : AuthStatus
    data object Unauthenticated : AuthStatus
}

@Factory
class CheckAuthStatusUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthStatus {
        val isLoggedIn = authRepository.isLoggedIn()
        if (!isLoggedIn) {
            return AuthStatus.Unauthenticated
        }
        val user = authRepository.getCurrentUser()
        return if (user != null) {
            AuthStatus.Authenticated(user)
        } else {
            when (val remoteResult = authRepository.fetchCurrentUserRemote()) {
                is DataResult.Success -> AuthStatus.Authenticated(remoteResult.data)
                is DataResult.Failure -> AuthStatus.Unauthenticated
            }
        }
    }
}

@Factory
class SendOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(mobile: String): DataResult<Int> {
        return authRepository.sendOtp(mobile)
    }
}

@Factory
class VerifyOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(mobile: String, otpCode: String): DataResult<User> {
        return authRepository.verifyOtp(mobile, otpCode)
    }
}
