package ir.aispeaking.subscription

import ir.aispeaking.domain.gateway.MarketBillingGateway
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.payment.MarketPurchaseResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.DirectCheckoutResult
import ir.aispeaking.domain.model.stage.PromoValidationResult
import ir.aispeaking.domain.model.stage.StageProgress
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.repository.PlatformMarketDetector
import ir.aispeaking.domain.repository.auth.AuthRepository
import ir.aispeaking.domain.repository.stage.StageProgressRepository
import ir.aispeaking.domain.repository.stage.SubscriptionRepository
import ir.aispeaking.domain.usecase.auth.IsLoggedInUseCase
import ir.aispeaking.domain.usecase.auth.SendOtpUseCase
import ir.aispeaking.domain.usecase.auth.VerifyOtpUseCase
import ir.aispeaking.domain.usecase.stage.CheckSubscriptionStatusUseCase
import ir.aispeaking.domain.usecase.stage.CreateDirectCheckoutUseCase
import ir.aispeaking.domain.usecase.stage.GetSubscriptionPlansUseCase
import ir.aispeaking.domain.usecase.stage.SubscribePlanUseCase
import ir.aispeaking.domain.usecase.stage.SyncGuestProgressUseCase
import ir.aispeaking.domain.usecase.stage.ValidateMarketPromoUseCase
import ir.aispeaking.domain.usecase.stage.VerifyMarketPurchaseUseCase
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
class SubscriptionViewModelGuestTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAnalyticsTracker : AnalyticsTracker {
        override fun logEvent(name: String, params: Map<String, Any?>) {}
        override fun setUserId(userId: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private class FakePlatformMarketDetector(
        var market: AppMarket = AppMarket.DIRECT_WEB
    ) : PlatformMarketDetector {
        override fun detectMarket(): AppMarket = market
    }

    private class FakeMarketBillingGateway : MarketBillingGateway {
        override val market: AppMarket = AppMarket.DIRECT_WEB
        override suspend fun startConnection(): Boolean = true
        override suspend fun disconnect() {}
        override suspend fun purchaseProduct(
            sku: String,
            payload: String?,
            dynamicPriceToken: String?
        ): MarketPurchaseResult = MarketPurchaseResult.Success(
            sku = sku,
            purchaseToken = "fake_token",
            market = AppMarket.DIRECT_WEB
        )
        override suspend fun consumePurchase(purchaseToken: String): Boolean = true
        override suspend fun queryUnconsumedPurchases(): List<MarketPurchaseResult.Success> = emptyList()
    }

    private class FakeSubscriptionRepository : SubscriptionRepository {
        var directCheckoutCallCount = 0
        var plans = listOf(
            SubscriptionPlan(
                id = "plan-3m",
                type = "3_MONTHS",
                titleFa = "۳ ماهه",
                durationDays = 90,
                priceTomans = 499_000L,
                discountPercent = 15,
                badge = "محبوب"
            )
        )

        override suspend fun getSubscriptionPlans(): DataResult<List<SubscriptionPlan>> {
            return DataResult.Success(plans)
        }

        override suspend fun getSubscriptionStatus(): DataResult<SubscriptionStatus> {
            return DataResult.Success(
                SubscriptionStatus(isSubscriber = false, planType = null, expiresAt = null, remainingDays = 0)
            )
        }

        override suspend fun subscribe(planId: String, promoCode: String?): DataResult<SubscriptionStatus> {
            return DataResult.Success(
                SubscriptionStatus(isSubscriber = true, planType = "3_MONTHS", expiresAt = "2026-12-01", remainingDays = 90)
            )
        }

        override suspend fun validatePromo(promoCode: String): DataResult<PromoValidationResult> {
            return DataResult.Success(
                PromoValidationResult(promoCode = promoCode, discountPercent = 10, message = "تخفیف ۱۰٪")
            )
        }

        override suspend fun validateMarketPromo(
            promoCode: String,
            planId: String,
            market: String
        ): DataResult<PromoValidationResult> {
            return DataResult.Success(
                PromoValidationResult(promoCode = promoCode, discountPercent = 10, message = "تخفیف ۱۰٪")
            )
        }

        override suspend fun verifyMarketPurchase(
            market: String,
            planId: String,
            sku: String,
            purchaseToken: String,
            orderId: String?,
            developerPayload: String?,
            promoCode: String?
        ): DataResult<SubscriptionStatus> {
            return DataResult.Success(
                SubscriptionStatus(isSubscriber = true, planType = "3_MONTHS", expiresAt = "2026-12-01", remainingDays = 90)
            )
        }

        override suspend fun createDirectCheckout(
            planId: String,
            promoCode: String?
        ): DataResult<DirectCheckoutResult> {
            directCheckoutCallCount++
            return DataResult.Success(
                DirectCheckoutResult(
                    paymentUrl = "https://gateway.aispeaking.ir/pay/fake-tx",
                    transactionId = "tx-123",
                    amountTomans = 499_000L
                )
            )
        }
    }

    private class FakeAuthRepository(
        var loggedIn: Boolean = false
    ) : AuthRepository {
        var sendOtpCallCount = 0
        var verifyOtpCallCount = 0

        override suspend fun getToken(): String? = if (loggedIn) "valid_token" else null
        override suspend fun saveToken(token: String) { loggedIn = token.isNotBlank() }
        override suspend fun isLoggedIn(): Boolean = loggedIn
        override suspend fun getCurrentUser(): User? = if (loggedIn) User(uid = "u1", nickName = "User", mobile = "09123456789") else null
        override suspend fun saveUser(user: User) {}
        override suspend fun getCurrentAccessTier(): AccessTier = if (loggedIn) AccessTier.REGISTERED_FREE else AccessTier.GUEST
        override suspend fun logout(): DataResult<Unit> {
            loggedIn = false
            return DataResult.Success(Unit)
        }
        override suspend fun sendOtp(mobile: String): DataResult<Int> {
            sendOtpCallCount++
            return DataResult.Success(120)
        }
        override suspend fun verifyOtp(mobile: String, otpCode: String): DataResult<User> {
            verifyOtpCallCount++
            loggedIn = true
            return DataResult.Success(User(uid = "u1", nickName = "User", mobile = mobile))
        }
        override suspend fun fetchCurrentUserRemote(): DataResult<User> {
            return DataResult.Success(User(uid = "u1", nickName = "User", mobile = "09123456789"))
        }
        override fun hasSeenIntroVideo(): Boolean = true
        override fun setHasSeenIntroVideo(seen: Boolean) {}
    }

    private class FakeStageProgressRepository : StageProgressRepository {
        var syncGuestCalled = false
        override suspend fun syncGuestProgress(): DataResult<List<StageProgress>> {
            syncGuestCalled = true
            return DataResult.Success(emptyList())
        }
        override suspend fun saveLocalGuestProgress(stageId: String, stars: Int, score: Int): DataResult<Unit> = DataResult.Success(Unit)
        override suspend fun getLocalGuestProgress(): DataResult<Map<String, StageProgress>> = DataResult.Success(emptyMap())
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        authRepo: FakeAuthRepository,
        subRepo: FakeSubscriptionRepository,
        stageProgressRepo: FakeStageProgressRepository
    ): SubscriptionViewModel {
        return SubscriptionViewModel(
            getSubscriptionPlansUseCase = GetSubscriptionPlansUseCase(subRepo),
            checkSubscriptionStatusUseCase = CheckSubscriptionStatusUseCase(subRepo),
            subscribePlanUseCase = SubscribePlanUseCase(subRepo),
            validateMarketPromoUseCase = ValidateMarketPromoUseCase(subRepo),
            verifyMarketPurchaseUseCase = VerifyMarketPurchaseUseCase(subRepo),
            createDirectCheckoutUseCase = CreateDirectCheckoutUseCase(subRepo),
            isLoggedInUseCase = IsLoggedInUseCase(authRepo),
            sendOtpUseCase = SendOtpUseCase(authRepo),
            verifyOtpUseCase = VerifyOtpUseCase(authRepo),
            syncGuestProgressUseCase = SyncGuestProgressUseCase(stageProgressRepo),
            marketDetector = FakePlatformMarketDetector(),
            billingGateway = FakeMarketBillingGateway(),
            analyticsTracker = FakeAnalyticsTracker(),
            crashReporter = DefaultCrashReporter()
        )
    }

    @Test
    fun testGuestUserPurchaseOpensRegisterBottomSheet() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository(loggedIn = false)
        val subRepo = FakeSubscriptionRepository()
        val stageProgressRepo = FakeStageProgressRepository()
        val viewModel = createViewModel(authRepo, subRepo, stageProgressRepo)
        advanceUntilIdle()

        // When guest taps purchase
        viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Register bottom sheet must be opened
        assertTrue(state.showRegisterDialog, "Expected register dialog to open for guest")
        assertEquals(1, state.registerStep)
        assertNull(state.registerError)
        assertFalse(state.isPurchasing)
        // Direct checkout must NOT have been called
        assertEquals(0, subRepo.directCheckoutCallCount)
    }

    @Test
    fun testSendRegisterOtpValidPhoneMovesToStep2() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository(loggedIn = false)
        val subRepo = FakeSubscriptionRepository()
        val stageProgressRepo = FakeStageProgressRepository()
        val viewModel = createViewModel(authRepo, subRepo, stageProgressRepo)
        advanceUntilIdle()

        viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan)
        advanceUntilIdle()

        viewModel.processIntent(SubscriptionIntent.SendRegisterOtp("09152413498"))
        advanceUntilIdle()

        assertEquals(1, authRepo.sendOtpCallCount)
        val state = viewModel.uiState.value
        assertEquals(2, state.registerStep)
        assertNull(state.registerError)
    }

    @Test
    fun testSendRegisterOtpInvalidPhoneShowsError() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository(loggedIn = false)
        val subRepo = FakeSubscriptionRepository()
        val stageProgressRepo = FakeStageProgressRepository()
        val viewModel = createViewModel(authRepo, subRepo, stageProgressRepo)
        advanceUntilIdle()

        viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan)
        advanceUntilIdle()

        viewModel.processIntent(SubscriptionIntent.SendRegisterOtp("12345"))
        advanceUntilIdle()

        assertEquals(0, authRepo.sendOtpCallCount)
        val state = viewModel.uiState.value
        assertEquals(1, state.registerStep)
        assertNotNull(state.registerError)
    }

    @Test
    fun testVerifyRegisterOtpSuccessClosesDialogAndCompletesPurchase() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository(loggedIn = false)
        val subRepo = FakeSubscriptionRepository()
        val stageProgressRepo = FakeStageProgressRepository()
        val viewModel = createViewModel(authRepo, subRepo, stageProgressRepo)
        advanceUntilIdle()

        // 1. Guest initiates purchase
        viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showRegisterDialog)

        // 2. Guest enters phone and receives OTP
        viewModel.processIntent(SubscriptionIntent.SendRegisterOtp("09152413498"))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.registerStep)

        // 3. Guest verifies OTP
        viewModel.processIntent(SubscriptionIntent.VerifyRegisterOtp("09152413498", "87799"))
        advanceUntilIdle()

        // Dialog should be dismissed, user is logged in, and purchase proceeded
        assertFalse(viewModel.uiState.value.showRegisterDialog)
        assertTrue(authRepo.loggedIn)
        assertTrue(stageProgressRepo.syncGuestCalled)
        assertEquals(1, subRepo.directCheckoutCallCount)
    }

    @Test
    fun testAuthenticatedUserBypassesRegisterDialog() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository(loggedIn = true)
        val subRepo = FakeSubscriptionRepository()
        val stageProgressRepo = FakeStageProgressRepository()
        val viewModel = createViewModel(authRepo, subRepo, stageProgressRepo)
        advanceUntilIdle()

        viewModel.processIntent(SubscriptionIntent.PurchaseSelectedPlan)
        advanceUntilIdle()

        // Register dialog should NOT be shown
        assertFalse(viewModel.uiState.value.showRegisterDialog)
        assertEquals(1, subRepo.directCheckoutCallCount)
    }
}
