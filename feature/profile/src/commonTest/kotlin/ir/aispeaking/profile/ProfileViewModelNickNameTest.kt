package ir.aispeaking.profile

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.user.SubscriptionSummary
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.domain.repository.auth.AuthRepository
import ir.aispeaking.domain.repository.theme.ThemeRepository
import ir.aispeaking.domain.repository.user.UserProfileRepository
import ir.aispeaking.domain.usecase.user.GetUserProfileUseCase
import ir.aispeaking.domain.usecase.user.UpdateUserProfileUseCase
import ir.aispeaking.utils.analytics.AnalyticsTracker
import ir.aispeaking.utils.crashlytics.DefaultCrashReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelNickNameTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeThemeRepository : ThemeRepository {
        override fun getThemeMode(): String = "System"
        override fun saveThemeMode(themeKey: String) {}
    }

    private class FakeUserProfileRepository(
        initialProfile: UserProfile,
        var shouldFailUpdate: Boolean = false
    ) : UserProfileRepository {
        var profile = initialProfile

        override suspend fun getUserProfile(): DataResult<UserProfile> = DataResult.Success(profile)

        override suspend fun updateProfile(input: UpdateProfileInput): DataResult<UserProfile> {
            if (shouldFailUpdate) {
                return DataResult.Failure(NetworkError.InternalServer(500))
            }
            profile = profile.copy(
                nickName = input.nickName ?: profile.nickName,
                avatar = input.avatar ?: profile.avatar,
                languageLevel = input.languageLevel ?: profile.languageLevel
            )
            return DataResult.Success(profile)
        }

        override suspend fun getGuestProfile(): UserProfile = profile
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun getToken(): String? = null
        override suspend fun saveToken(token: String) {}
        override suspend fun isLoggedIn(): Boolean = false
        override suspend fun getCurrentUser(): User? = null
        override suspend fun saveUser(user: User) {}
        override suspend fun getCurrentAccessTier(): AccessTier = AccessTier.GUEST
        override suspend fun logout(): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun sendOtp(mobile: String): DataResult<Int> = DataResult.Success(120)
        override suspend fun verifyOtp(mobile: String, otpCode: String): DataResult<User> =
            DataResult.Success(User("u1", "User", mobile = mobile))
        override suspend fun fetchCurrentUserRemote(): DataResult<User> =
            DataResult.Success(User("u1", "User", mobile = "09123456789"))
        override fun hasSeenIntroVideo(): Boolean = true
        override fun setHasSeenIntroVideo(seen: Boolean) {}
    }

    private class FakeAnalyticsTracker : AnalyticsTracker {
        override fun logEvent(name: String, params: Map<String, Any?>) {}
        override fun setUserId(userId: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repo: UserProfileRepository): ProfileViewModel {
        return ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(repo),
            updateUserProfileUseCase = UpdateUserProfileUseCase(repo),
            authRepository = FakeAuthRepository(),
            themeRepository = FakeThemeRepository(),
            analyticsTracker = FakeAnalyticsTracker(),
            crashReporter = DefaultCrashReporter()
        )
    }

    @Test
    fun testValidNickNameUpdateForAuthenticatedUser() = runTest(testDispatcher) {
        val repo = FakeUserProfileRepository(
            UserProfile(
                id = "u1",
                phoneNumber = "09152413498",
                nickName = "Learner",
                subscription = SubscriptionSummary(isSubscriber = false),
                isGuest = false
            )
        )
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onEditNameClicked()
        assertTrue(viewModel.uiState.value.showEditNameDialog)

        viewModel.onSaveNickName("  مهدی  ")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEditNameDialog)
        assertNull(viewModel.uiState.value.editNameError)
        assertEquals("مهدی", viewModel.uiState.value.user?.nickName)
        assertEquals("مهدی", viewModel.uiState.value.user?.displayName)
    }

    @Test
    fun testValidNickNameUpdateForGuestUser() = runTest(testDispatcher) {
        val repo = FakeUserProfileRepository(
            UserProfile(
                id = "guest",
                phoneNumber = "",
                nickName = "",
                isGuest = true
            )
        )
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onEditNameClicked()
        viewModel.onSaveNickName("سارا")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEditNameDialog)
        assertEquals("سارا", viewModel.uiState.value.user?.nickName)
        assertEquals("سارا", viewModel.uiState.value.user?.displayName)
    }

    @Test
    fun testInvalidShortNickNameSetsInlineErrorAndKeepsDialogOpen() = runTest(testDispatcher) {
        val repo = FakeUserProfileRepository(
            UserProfile(
                id = "u1",
                phoneNumber = "09152413498",
                nickName = "Ali",
                isGuest = false
            )
        )
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onEditNameClicked()
        viewModel.onSaveNickName("ا")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showEditNameDialog)
        assertNotNull(viewModel.uiState.value.editNameError)
        assertEquals("Ali", viewModel.uiState.value.user?.nickName)

        viewModel.onClearEditNameError()
        assertNull(viewModel.uiState.value.editNameError)
    }

    @Test
    fun testUpdateFailureSetsErrorMessage() = runTest(testDispatcher) {
        val repo = FakeUserProfileRepository(
            initialProfile = UserProfile(
                id = "u1",
                phoneNumber = "09152413498",
                nickName = "Ali",
                isGuest = false
            ),
            shouldFailUpdate = true
        )
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onEditNameClicked()
        viewModel.onSaveNickName("رضا")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEditNameDialog)
        assertEquals("خطا در ذخیره تغییرات", viewModel.uiState.value.errorMessage)
        assertEquals("Ali", viewModel.uiState.value.user?.nickName)
    }
}
