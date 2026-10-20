package ir.aispeaking.profile

import ir.aispeaking.domain.model.data_result.DataResult
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
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.them.ThemeController
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

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelThemeTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeThemeRepository(
        var savedTheme: String = "System"
    ) : ThemeRepository {
        override fun getThemeMode(): String = savedTheme
        override fun saveThemeMode(themeKey: String) {
            savedTheme = themeKey
        }
    }

    private class FakeUserProfileRepository : UserProfileRepository {
        var profile = UserProfile(
            id = "u1",
            phoneNumber = "09123456789",
            nickName = "Test User",
            avatar = "avatar_g1",
            score = 100,
            totalStars = 5,
            completedStagesCount = 2,
            languageLevel = "A1",
            subscription = SubscriptionSummary(isSubscriber = false),
            isGuest = false
        )

        override suspend fun getUserProfile(): DataResult<UserProfile> {
            return DataResult.Success(profile)
        }

        override suspend fun updateProfile(input: UpdateProfileInput): DataResult<UserProfile> {
            profile = profile.copy(
                nickName = input.nickName ?: profile.nickName,
                avatar = input.avatar ?: profile.avatar
            )
            return DataResult.Success(profile)
        }

        override suspend fun getGuestProfile(): UserProfile = profile
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun getToken(): String? = "token"
        override suspend fun saveToken(token: String) {}
        override suspend fun isLoggedIn(): Boolean = true
        override suspend fun getCurrentUser(): User? = null
        override suspend fun saveUser(user: User) {}
        override suspend fun getCurrentAccessTier(): AccessTier = AccessTier.GUEST
        override suspend fun logout(): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun sendOtp(mobile: String): DataResult<Int> = DataResult.Success(120)
        override suspend fun verifyOtp(mobile: String, otpCode: String): DataResult<User> = DataResult.Success(User("u1", "User", mobile = mobile))
        override suspend fun fetchCurrentUserRemote(): DataResult<User> = DataResult.Success(User("u1", "User", mobile = "09123456789"))
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    private class FakeAnalyticsTracker : AnalyticsTracker {
        override fun logEvent(name: String, params: Map<String, Any?>) {}
        override fun setUserId(userId: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialThemeLoadedFromRepository() = runTest(testDispatcher) {
        val fakeThemeRepo = FakeThemeRepository(savedTheme = "Dark")
        val userRepo = FakeUserProfileRepository()
        val authRepo = FakeAuthRepository()

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(userRepo),
            updateUserProfileUseCase = UpdateUserProfileUseCase(userRepo),
            authRepository = authRepo,
            themeRepository = fakeThemeRepo,
            analyticsTracker = FakeAnalyticsTracker(),
            crashReporter = DefaultCrashReporter()
        )

        advanceUntilIdle()

        assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
        assertEquals(ThemeMode.Dark, ThemeController.themeMode.value)
    }

    @Test
    fun testThemeModeSelectionUpdatesStateAndPersists() = runTest(testDispatcher) {
        val fakeThemeRepo = FakeThemeRepository(savedTheme = "System")
        val userRepo = FakeUserProfileRepository()
        val authRepo = FakeAuthRepository()

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(userRepo),
            updateUserProfileUseCase = UpdateUserProfileUseCase(userRepo),
            authRepository = authRepo,
            themeRepository = fakeThemeRepo,
            analyticsTracker = FakeAnalyticsTracker(),
            crashReporter = DefaultCrashReporter()
        )

        advanceUntilIdle()
        assertEquals(ThemeMode.System, viewModel.uiState.value.themeMode)

        // Select Light Mode
        viewModel.onThemeSelected(ThemeMode.Light)
        advanceUntilIdle()

        assertEquals(ThemeMode.Light, viewModel.uiState.value.themeMode)
        assertEquals(ThemeMode.Light, ThemeController.themeMode.value)
        assertEquals("Light", fakeThemeRepo.savedTheme)

        // Select Dark Mode
        viewModel.onThemeSelected(ThemeMode.Dark)
        advanceUntilIdle()

        assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
        assertEquals(ThemeMode.Dark, ThemeController.themeMode.value)
        assertEquals("Dark", fakeThemeRepo.savedTheme)

        // Select System Mode
        viewModel.onThemeSelected(ThemeMode.System)
        advanceUntilIdle()

        assertEquals(ThemeMode.System, viewModel.uiState.value.themeMode)
        assertEquals(ThemeMode.System, ThemeController.themeMode.value)
        assertEquals("System", fakeThemeRepo.savedTheme)
    }
}
