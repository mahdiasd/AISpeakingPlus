package ir.aispeaking.domain.repository.auth

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.user.User

interface AuthRepository {
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun saveUser(user: User)
    suspend fun getCurrentAccessTier(): AccessTier
    suspend fun logout(): DataResult<Unit>
    suspend fun sendOtp(mobile: String): DataResult<Int>
    suspend fun verifyOtp(mobile: String, otpCode: String): DataResult<User>
    suspend fun fetchCurrentUserRemote(): DataResult<User>
}
