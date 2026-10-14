package ir.speaking.feature.admin.promo

import ir.speaking.feature.admin.promo.model.AdminPromoCodeUpsertRequest
import ir.speaking.feature.admin.promo.service.AdminPromoService
import ir.speaking.feature.subscription.db.PromoCodeRedemptionTable
import ir.speaking.feature.subscription.db.PromoCodeTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AdminPromoServiceTest {

    private lateinit var db: Database
    private val promoService = AdminPromoService()

    @BeforeEach
    fun setup() {
        db = Database.connect(
            url = "jdbc:h2:mem:test_promo_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        transaction(db) {
            SchemaUtils.create(
                UserTable,
                SubscriptionTable,
                PromoCodeTable,
                PromoCodeRedemptionTable
            )
        }
    }

    @AfterEach
    fun teardown() {
        transaction(db) {
            SchemaUtils.drop(
                PromoCodeRedemptionTable,
                PromoCodeTable,
                SubscriptionTable,
                UserTable
            )
        }
    }

    @Test
    fun `test promo code creation and retrieval`() = runBlocking {
        val req = AdminPromoCodeUpsertRequest(
            code = "TESTPERCENT30",
            title = "Test Campaign",
            discountType = "PERCENTAGE",
            discountValue = 30,
            maxDiscountCap = 100_000L,
            applicablePlans = listOf("plan-3m", "plan-6m"),
            maxUses = 50,
            isOncePerUser = true,
            isActive = true
        )

        val created = promoService.createPromoCode(null, req)
        assertEquals("TESTPERCENT30", created.code)
        assertEquals(30, created.discountValue)
        assertEquals(50, created.maxUses)
        assertTrue(created.isActive)

        // Validation for matching plan
        val validForPlan3m = promoService.validateForUserAndPlan(
            code = "TESTPERCENT30",
            userId = null,
            planId = "plan-3m"
        )
        assertTrue(validForPlan3m.isValid)
        assertEquals(30, validForPlan3m.discountPercent)

        // Validation for non-matching plan should fail
        val invalidForPlan1m = promoService.validateForUserAndPlan(
            code = "TESTPERCENT30",
            userId = null,
            planId = "plan-1m"
        )
        assertFalse(invalidForPlan1m.isValid)
    }

    @Test
    fun `test fixed amount promo calculation and redemption`() = runBlocking {
        val req = AdminPromoCodeUpsertRequest(
            code = "DISCOUNT50K",
            title = "Fifty Thousand Off",
            discountType = "FIXED_AMOUNT",
            discountValue = 50_000L,
            minOrderAmount = 100_000L,
            applicablePlans = emptyList(), // ALL
            maxUses = 2,
            isOncePerUser = true,
            isActive = true
        )

        val created = promoService.createPromoCode(null, req)
        assertNotNull(created)

        // Create test user
        var userId: UUID = UUID.randomUUID()
        transaction(db) {
            val insertedId = UserTable.insertAndGetId {
                it[mobile] = "09129990000"
                it[nickName] = "Test Promo User"
            }
            userId = insertedId.value
        }

        // 1st redemption should succeed
        val val1 = promoService.validateForUserAndPlan(
            code = "DISCOUNT50K",
            userId = userId,
            planId = "plan-1m"
        )
        assertTrue(val1.isValid)
        assertEquals(50_000L, val1.discountAmountTomans)

        promoService.recordRedemption(
            promoCodeId = UUID.fromString(created.id),
            userId = userId,
            subscriptionId = null,
            planId = "plan-1m",
            originalPrice = val1.originalPriceTomans,
            discountAmount = val1.discountAmountTomans,
            finalPrice = val1.finalPriceTomans
        )

        // 2nd redemption for SAME user should fail due to isOncePerUser
        val val2 = promoService.validateForUserAndPlan(
            code = "DISCOUNT50K",
            userId = userId,
            planId = "plan-1m"
        )
        assertFalse(val2.isValid)
        assertTrue(val2.message.contains("یک بار"))
    }

    @Test
    fun `test soft delete and status toggle`() = runBlocking {
        val req = AdminPromoCodeUpsertRequest(
            code = "TEMPORARY10",
            discountType = "PERCENTAGE",
            discountValue = 10,
            isActive = true
        )

        val created = promoService.createPromoCode(null, req)
        val id = UUID.fromString(created.id)

        // Toggle inactive
        val disabled = promoService.toggleStatus(id, false)
        assertFalse(disabled.isActive)

        val valDisabled = promoService.validateForUserAndPlan("TEMPORARY10")
        assertFalse(valDisabled.isValid)

        // Soft delete
        val deleted = promoService.deletePromoCode(id)
        assertTrue(deleted)

        val list = promoService.getPromoCodes()
        assertFalse(list.items.any { it.code == "TEMPORARY10" })
    }

    @Test
    fun `test plan aliases normalization and validation constraints`() = runBlocking {
        // Plan alias test
        val req = AdminPromoCodeUpsertRequest(
            code = "ALIAS10",
            discountType = "PERCENTAGE",
            discountValue = 10,
            applicablePlans = listOf("1_MONTH"),
            isActive = true
        )
        val created = promoService.createPromoCode(null, req)
        assertNotNull(created)

        // Validate using "plan-1m" alias
        val validWithPlanId = promoService.validateForUserAndPlan("ALIAS10", planId = "plan-1m")
        assertTrue(validWithPlanId.isValid)

        // Validate using "1_MONTH" alias
        val validWithEnum = promoService.validateForUserAndPlan("ALIAS10", planId = "1_MONTH")
        assertTrue(validWithEnum.isValid)

        // Invalid percentage > 100 should throw
        var threwPercentage = false
        try {
            promoService.createPromoCode(null, req.copy(code = "INVALID101", discountValue = 101))
        } catch (_: IllegalArgumentException) {
            threwPercentage = true
        }
        assertTrue(threwPercentage)

        // Invalid dates (expires before starts) should throw
        var threwDates = false
        try {
            promoService.createPromoCode(
                null,
                req.copy(
                    code = "INVALIDDATES",
                    startsAt = "2026-10-15T12:00:00Z",
                    expiresAt = "2026-10-10T12:00:00Z"
                )
            )
        } catch (_: IllegalArgumentException) {
            threwDates = true
        }
        assertTrue(threwDates)
    }
}
