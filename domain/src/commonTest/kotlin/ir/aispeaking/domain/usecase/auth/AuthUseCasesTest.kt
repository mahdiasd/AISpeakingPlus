package ir.aispeaking.domain.usecase.auth

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.repository.auth.AuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthUseCasesTest {

    private class FakeAuthRepository(
        var token: String? = null,
        var tier: AccessTier = AccessTier.GUEST,
        var currentUser: User? = null
    ) : AuthRepository {
        override suspend fun getToken(): String? = token
        override suspend fun saveToken(token: String) { this.token = token }
        override suspend fun isLoggedIn(): Boolean = !token.isNullOrBlank()
        override suspend fun getCurrentUser(): User? = currentUser
        override suspend fun saveUser(user: User) { this.currentUser = user }
        override suspend fun getCurrentAccessTier(): AccessTier = tier
        override suspend fun logout(): DataResult<Unit> {
            token = null
            currentUser = null
            tier = AccessTier.GUEST
            return DataResult.Success(Unit)
        }
        override suspend fun sendOtp(mobile: String): DataResult<Int> = DataResult.Success(120)
        override suspend fun verifyOtp(mobile: String, otpCode: String): DataResult<User> =
            currentUser?.let { DataResult.Success(it) } ?: DataResult.Failure(ir.aispeaking.domain.model.error.NetworkError.Unauthorized())
        override suspend fun fetchCurrentUserRemote(): DataResult<User> =
            currentUser?.let { DataResult.Success(it) } ?: DataResult.Failure(ir.aispeaking.domain.model.error.NetworkError.NotFound())
    }

    @Test
    fun testAccessTierResolution() = runTest {
        val repo = FakeAuthRepository(tier = AccessTier.SUBSCRIBER)
        val useCase = GetCurrentAccessTierUseCase(repo)

        assertEquals(AccessTier.SUBSCRIBER, useCase())
    }

    @Test
    fun testAuthSessionManagement() = runTest {
        val repo = FakeAuthRepository(
            token = "jwt_token_123",
            tier = AccessTier.REGISTERED_FREE,
            currentUser = User(uid = "u1", nickName = "Learner", mobile = "09121234567")
        )
        val getTokenUseCase = GetAuthTokenUseCase(repo)
        val isLoggedInUseCase = IsLoggedInUseCase(repo)
        val getCurrentUserUseCase = GetCurrentUserUseCase(repo)
        val logoutUseCase = LogoutUseCase(repo)

        assertEquals("jwt_token_123", getTokenUseCase())
        assertTrue(isLoggedInUseCase())
        assertEquals("Learner", getCurrentUserUseCase()?.nickName)

        val logoutResult = logoutUseCase()
        assertEquals(DataResult.Success(Unit), logoutResult)
        assertEquals(null, getTokenUseCase())
        assertEquals(AccessTier.GUEST, repo.getCurrentAccessTier())
    }
}
