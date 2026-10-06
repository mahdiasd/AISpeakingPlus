package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus
import ir.aispeaking.domain.repository.stage.SubscriptionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SubscribePlanUseCaseTest {

    private class FakeSubscriptionRepository(
        var shouldSucceed: Boolean = true
    ) : SubscriptionRepository {
        var lastPlanId: String? = null
        var lastPromoCode: String? = null

        override suspend fun getSubscriptionPlans(): DataResult<List<SubscriptionPlan>> {
            return DataResult.Success(emptyList())
        }

        override suspend fun getSubscriptionStatus(): DataResult<SubscriptionStatus> {
            return DataResult.Success(SubscriptionStatus(isSubscriber = false))
        }

        override suspend fun subscribe(planId: String, promoCode: String?): DataResult<SubscriptionStatus> {
            lastPlanId = planId
            lastPromoCode = promoCode
            return if (shouldSucceed) {
                DataResult.Success(
                    SubscriptionStatus(
                        isSubscriber = true,
                        planType = "3_MONTHS",
                        expiresAt = "2027-01-01T00:00:00Z",
                        remainingDays = 90
                    )
                )
            } else {
                DataResult.Failure(NetworkError.InternalServer(message = "Failed to subscribe"))
            }
        }
    }

    @Test
    fun testSubscribePlanSuccess() = runTest {
        val fakeRepo = FakeSubscriptionRepository(shouldSucceed = true)
        val useCase = SubscribePlanUseCase(fakeRepo)

        val result = useCase(planId = "plan-3m", promoCode = "GOLD20")

        assertIs<DataResult.Success<SubscriptionStatus>>(result)
        assertEquals(true, result.data.isSubscriber)
        assertEquals(90, result.data.remainingDays)
        assertEquals("plan-3m", fakeRepo.lastPlanId)
        assertEquals("GOLD20", fakeRepo.lastPromoCode)
    }

    @Test
    fun testSubscribePlanFailure() = runTest {
        val fakeRepo = FakeSubscriptionRepository(shouldSucceed = false)
        val useCase = SubscribePlanUseCase(fakeRepo)

        val result = useCase(planId = "plan-1m", promoCode = null)

        assertIs<DataResult.Failure<SubscriptionStatus>>(result)
        assertEquals("plan-1m", fakeRepo.lastPlanId)
        assertEquals(null, fakeRepo.lastPromoCode)
    }
}
