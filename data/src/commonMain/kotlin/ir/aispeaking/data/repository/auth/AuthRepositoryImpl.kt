package ir.aispeaking.data.repository.auth

import ir.aispeaking.data.mapper.user.toDomain
import ir.aispeaking.data.mapper.user.toStorage
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.repository.auth.AuthRepository
import ir.aispeaking.domain.repository.stage.SubscriptionRepository
import ir.aispeaking.network.api.auth.AuthApi
import ir.aispeaking.network.model.auth.dto.SendOtpRequestDto
import ir.aispeaking.network.model.auth.dto.VerifyOtpRequestDto
import ir.aispeaking.storage.preferences.clear.ClearSharedPreferences
import ir.aispeaking.storage.preferences.token.TokenPreferences
import ir.aispeaking.storage.preferences.user.UserPreferences
import org.koin.core.annotation.Single

@Single
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenPreferences: TokenPreferences,
    private val userPreferences: UserPreferences,
    private val clearSharedPreferences: ClearSharedPreferences,
    private val subscriptionRepository: SubscriptionRepository
) : AuthRepository {

    override suspend fun getToken(): String? {
        val token = tokenPreferences.read()
        return token.takeIf { it.isNotBlank() }
    }

    override suspend fun saveToken(token: String) {
        tokenPreferences.save(token)
    }

    override suspend fun isLoggedIn(): Boolean {
        val token = tokenPreferences.read()
        return token.isNotBlank()
    }

    override suspend fun getCurrentUser(): User? {
        return userPreferences.read()?.toDomain()
    }

    override suspend fun saveUser(user: User) {
        userPreferences.save(user.toStorage())
    }

    override suspend fun getCurrentAccessTier(): AccessTier {
        val token = tokenPreferences.read()
        if (token.isBlank()) {
            return AccessTier.GUEST
        }

        val subResult = subscriptionRepository.getSubscriptionStatus()
        return if (subResult is DataResult.Success && subResult.data.isSubscriber) {
            AccessTier.SUBSCRIBER
        } else {
            AccessTier.REGISTERED_FREE
        }
    }

    override suspend fun logout(): DataResult<Unit> {
        return try {
            clearSharedPreferences.clear()
            DataResult.Success(Unit)
        } catch (e: Throwable) {
            DataResult.Failure(
                ir.aispeaking.domain.model.error.NetworkError.Unknown(message = e.message ?: "Logout failed")
            )
        }
    }

    override suspend fun sendOtp(mobile: String): DataResult<Int> {
        val result = safeCall { authApi.sendOtp(SendOtpRequestDto(mobile = mobile)) }
        return when (result) {
            is DataResult.Success -> DataResult.Success(result.data.expiresInSeconds)
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun verifyOtp(mobile: String, otpCode: String): DataResult<User> {
        val result = safeCall { authApi.verifyOtp(VerifyOtpRequestDto(mobile = mobile, otpCode = otpCode)) }
        return when (result) {
            is DataResult.Success -> {
                val token = result.data.token
                tokenPreferences.save(token)
                val user = result.data.user.toDomain()
                userPreferences.save(user.toStorage())
                DataResult.Success(user)
            }
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun fetchCurrentUserRemote(): DataResult<User> {
        val result = safeCall { authApi.getMe() }
        return when (result) {
            is DataResult.Success -> {
                val user = result.data.toDomain()
                userPreferences.save(user.toStorage())
                DataResult.Success(user)
            }
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }
}
