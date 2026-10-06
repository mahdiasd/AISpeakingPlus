package ir.aispeaking.data.mapper.user

import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.network.model.user.dto.SubscriptionSummaryDto
import ir.aispeaking.network.model.user.dto.UserProfileResponseDto
import ir.aispeaking.storage.model.user.SharedPrefUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserProfileMappersTest {

    @Test
    fun testSubscriptionSummaryDtoToDomain() {
        val dto = SubscriptionSummaryDto(
            isSubscriber = true,
            planType = "3_MONTHS",
            planTitleFa = "اشتراک ۳ ماهه",
            startedAt = "2026-10-01T00:00:00Z",
            expiresAt = "2027-01-01T00:00:00Z",
            remainingDays = 87
        )
        val domain = dto.toDomain()
        assertTrue(domain.isSubscriber)
        assertEquals("3_MONTHS", domain.planType)
        assertEquals("اشتراک ۳ ماهه", domain.planTitleFa)
        assertEquals(87, domain.remainingDays)
        assertFalse(domain.isExpiringSoon)
    }

    @Test
    fun testUserProfileResponseDtoToDomain() {
        val dto = UserProfileResponseDto(
            id = "user-abc-123",
            phoneNumber = "09121234567",
            nickName = "مریم",
            firstName = "Maryam",
            lastName = "Rad",
            avatar = "avatar_g4",
            score = 340,
            totalStars = 15,
            completedStagesCount = 5,
            languageLevel = "B1",
            subscription = SubscriptionSummaryDto(
                isSubscriber = false,
                remainingDays = 0
            )
        )
        val domain = dto.toDomain()
        assertEquals("user-abc-123", domain.id)
        assertEquals("09121234567", domain.phoneNumber)
        assertEquals("مریم", domain.nickName)
        assertEquals("avatar_g4", domain.avatar)
        assertEquals(340, domain.score)
        assertEquals(15, domain.totalStars)
        assertEquals(5, domain.completedStagesCount)
        assertEquals("B1", domain.languageLevel)
        assertFalse(domain.subscription.isSubscriber)
        assertFalse(domain.isGuest)
        assertEquals("مریم", domain.displayName)
    }

    @Test
    fun testUpdateProfileInputToDtoTrimming() {
        val input = UpdateProfileInput(
            nickName = "  رضا تستی  ",
            avatar = " avatar_g9 ",
            firstName = "  Ali  ",
            lastName = null,
            languageLevel = " B2 "
        )
        val dto = input.toDto()
        assertEquals("رضا تستی", dto.nickName)
        assertEquals("avatar_g9", dto.avatar)
        assertEquals("Ali", dto.firstName)
        assertNull(dto.lastName)
        assertEquals("B2", dto.languageLevel)
    }

    @Test
    fun testSharedPrefUserToGuestDomainProfile() {
        val prefUser = SharedPrefUser(
            uid = "",
            nickName = "",
            firstName = "",
            lastName = "",
            mobile = "",
            gender = null,
            languageLevel = "A1",
            age = 0,
            score = 0,
            avatar = ""
        )
        val guestProfile = prefUser.toGuestDomainProfile(guestStars = 6, guestScore = 90)
        assertTrue(guestProfile.isGuest)
        assertEquals("زبان‌آموز مهمان", guestProfile.nickName)
        assertEquals("default_avatar", guestProfile.avatar)
        assertEquals(6, guestProfile.totalStars)
        assertEquals(90, guestProfile.score)
        assertEquals(1, guestProfile.completedStagesCount)
        assertFalse(guestProfile.subscription.isSubscriber)
    }
}
