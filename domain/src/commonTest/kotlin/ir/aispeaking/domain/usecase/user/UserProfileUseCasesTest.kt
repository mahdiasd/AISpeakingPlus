package ir.aispeaking.domain.usecase.user

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.SubscriptionSummary
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.domain.repository.user.UserProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProfileUseCasesTest {

    private class FakeUserProfileRepository : UserProfileRepository {
        var profile = UserProfile(
            id = "user-123",
            phoneNumber = "09123456789",
            nickName = "Ali",
            avatar = "avatar_g1",
            score = 150,
            totalStars = 9,
            completedStagesCount = 3,
            languageLevel = "A2",
            subscription = SubscriptionSummary(
                isSubscriber = true,
                planType = "1_MONTH",
                planTitleFa = "اشتراک ۱ ماهه",
                remainingDays = 25
            ),
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

        override suspend fun getGuestProfile(): UserProfile {
            return UserProfile(
                id = "guest",
                phoneNumber = "",
                nickName = "زبان‌آموز مهمان",
                avatar = "avatar_g1",
                score = 30,
                totalStars = 3,
                completedStagesCount = 1,
                languageLevel = "A1",
                subscription = SubscriptionSummary(isSubscriber = false),
                isGuest = true
            )
        }
    }

    @Test
    fun testGetUserProfileUseCase() = runTest {
        val repo = FakeUserProfileRepository()
        val useCase = GetUserProfileUseCase(repo)

        val result = useCase()
        assertTrue(result is DataResult.Success)
        val data = (result as DataResult.Success).data
        assertEquals("Ali", data.nickName)
        assertEquals("09123456789", data.phoneNumber)
        assertEquals(9, data.totalStars)
        assertEquals(150, data.score)
        assertEquals(3, data.completedStagesCount)
        assertTrue(data.subscription.isSubscriber)
        assertEquals(25, data.subscription.remainingDays)
    }

    @Test
    fun testUpdateUserProfileUseCase() = runTest {
        val repo = FakeUserProfileRepository()
        val useCase = UpdateUserProfileUseCase(repo)

        val updateResult = useCase(UpdateProfileInput(nickName = "رضا", avatar = "avatar_g5"))
        assertTrue(updateResult is DataResult.Success)
        val data = (updateResult as DataResult.Success).data
        assertEquals("رضا", data.nickName)
        assertEquals("avatar_g5", data.avatar)
    }

    @Test
    fun testGuestProfileProperties() = runTest {
        val repo = FakeUserProfileRepository()
        val guest = repo.getGuestProfile()

        assertTrue(guest.isGuest)
        assertEquals("زبان‌آموز مهمان", guest.displayName)
        assertEquals(3, guest.totalStars)
        assertEquals(false, guest.subscription.isSubscriber)
    }
}
