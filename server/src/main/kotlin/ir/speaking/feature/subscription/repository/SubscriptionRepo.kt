package ir.speaking.feature.subscription.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.subscription.db.SubscriptionTable
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SortOrder
import org.koin.core.annotation.Single
import java.util.*
import ir.speaking.feature.subscription.db.MarketPaymentTable
import ir.speaking.feature.subscription.service.MarketVerificationService

data class VerifyMarketPurchaseParams(
    val userId: UUID,
    val market: String,
    val planId: String,
    val sku: String,
    val purchaseToken: String,
    val orderId: String? = null,
    val developerPayload: String? = null,
    val promoCode: String? = null,
    val bonusDays: Int = 0,
    val amountPaidTomans: Long = 0L
)

data class UserSubscriptionInfo(
    val isSubscriber: Boolean,
    val planType: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val remainingDays: Int = 0
)

data class VerifyMarketPurchaseResult(
    val isSuccess: Boolean,
    val subscriptionInfo: UserSubscriptionInfo? = null,
    val transactionId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

@Single
class SubscriptionRepo(
    private val marketVerificationService: MarketVerificationService
) {
    constructor() : this(MarketVerificationService())

    suspend fun hasActiveSubscription(userId: UUID): Boolean = suspendTransaction {
        val now = Clock.System.now()
        val activeRow = SubscriptionTable.selectAll()
            .where {
                (SubscriptionTable.userId eq userId) and
                (SubscriptionTable.status eq "ACTIVE") and
                (SubscriptionTable.expiresAt greater now)
            }
            .firstOrNull()

        activeRow != null
    }

    suspend fun getSubscriptionInfo(userId: UUID): UserSubscriptionInfo = suspendTransaction {
        val now = Clock.System.now()
        val activeRow = SubscriptionTable.selectAll()
            .where {
                (SubscriptionTable.userId eq userId) and
                (SubscriptionTable.status eq "ACTIVE") and
                (SubscriptionTable.expiresAt greater now)
            }
            .orderBy(
                SubscriptionTable.expiresAt to org.jetbrains.exposed.sql.SortOrder.DESC,
                SubscriptionTable.createdAt to org.jetbrains.exposed.sql.SortOrder.DESC
            )
            .firstOrNull()

        if (activeRow != null) {
            val startedAt = activeRow[SubscriptionTable.startedAt]
            val expiresAt = activeRow[SubscriptionTable.expiresAt]
            val remainingDays = maxOf(0, (expiresAt.epochSeconds - now.epochSeconds) / (24 * 3600)).toInt()
            UserSubscriptionInfo(
                isSubscriber = true,
                planType = activeRow[SubscriptionTable.planType],
                startedAt = startedAt.toString(),
                expiresAt = expiresAt.toString(),
                remainingDays = remainingDays
            )
        } else {
            UserSubscriptionInfo(isSubscriber = false)
        }
    }

    suspend fun activateSubscription(
        userId: UUID,
        planType: String,
        durationDays: Int,
        grantSource: String = "USER_PURCHASE",
        reason: String? = null
    ): UserSubscriptionInfo = suspendTransaction {
        val now = Clock.System.now()
        val activeRow = SubscriptionTable.selectAll()
            .where {
                (SubscriptionTable.userId eq userId) and
                (SubscriptionTable.status eq "ACTIVE") and
                (SubscriptionTable.expiresAt greater now)
            }
            .orderBy(
                SubscriptionTable.expiresAt to org.jetbrains.exposed.sql.SortOrder.DESC,
                SubscriptionTable.createdAt to org.jetbrains.exposed.sql.SortOrder.DESC
            )
            .firstOrNull()

        val baseTime = if (activeRow != null) {
            activeRow[SubscriptionTable.expiresAt]
        } else {
            now
        }
        val effectiveStartedAt = activeRow?.get(SubscriptionTable.startedAt) ?: now

        val effectiveDays = if (durationDays <= 0) 30 else durationDays
        val newExpiresAt = baseTime.plus(effectiveDays, kotlinx.datetime.DateTimeUnit.DAY, kotlinx.datetime.TimeZone.UTC)

        // Do NOT mark existing active subscription(s) as EXPIRED; insert cumulative renewal row with extended expiresAt
        SubscriptionTable.insertAndGetId {
            it[SubscriptionTable.userId] = userId
            it[SubscriptionTable.planType] = planType.uppercase()
            it[SubscriptionTable.startedAt] = effectiveStartedAt
            it[SubscriptionTable.expiresAt] = newExpiresAt
            it[SubscriptionTable.status] = "ACTIVE"
            it[SubscriptionTable.grantSource] = grantSource
            it[SubscriptionTable.grantReason] = reason
        }

        val remainingDays = maxOf(0, (newExpiresAt.epochSeconds - now.epochSeconds) / (24 * 3600)).toInt()
        UserSubscriptionInfo(
            isSubscriber = true,
            planType = planType.uppercase(),
            startedAt = effectiveStartedAt.toString(),
            expiresAt = newExpiresAt.toString(),
            remainingDays = remainingDays
        )
    }

    suspend fun verifyMarketPurchase(params: VerifyMarketPurchaseParams): VerifyMarketPurchaseResult {
        // 1. Check existing purchase token in database (Replay Attack & Idempotency)
        val existingTx = suspendTransaction {
            MarketPaymentTable.selectAll()
                .where { MarketPaymentTable.purchaseToken eq params.purchaseToken }
                .firstOrNull()
        }

        if (existingTx != null) {
            val existingUserId = existingTx[MarketPaymentTable.userId].value
            val existingStatus = existingTx[MarketPaymentTable.status]
            if (existingStatus == "VERIFIED" && existingUserId == params.userId) {
                // Idempotent recovery! Return the current subscription info without duplicate grant
                val info = getSubscriptionInfo(params.userId)
                return VerifyMarketPurchaseResult(
                    isSuccess = true,
                    subscriptionInfo = info,
                    transactionId = existingTx[MarketPaymentTable.id].value.toString()
                )
            } else {
                return VerifyMarketPurchaseResult(
                    isSuccess = false,
                    errorCode = "INVALID_OR_DUPLICATE_PURCHASE_TOKEN",
                    errorMessage = "این رسید قبلاً استفاده شده است یا نامعتبر می‌باشد"
                )
            }
        }

        // 2. Insert PENDING record in market_payment_transactions
        val txId = suspendTransaction {
            MarketPaymentTable.insertAndGetId {
                it[userId] = params.userId
                it[market] = params.market.uppercase()
                it[sku] = params.sku
                it[purchaseToken] = params.purchaseToken
                it[developerPayload] = params.developerPayload
                it[orderId] = params.orderId
                it[amountPaidTomans] = params.amountPaidTomans
                it[promoCode] = params.promoCode
                it[bonusDays] = params.bonusDays
                it[status] = "PENDING"
            }
        }

        // 3. Verify with market server API
        val verifyResult = marketVerificationService.verifyPurchase(
            market = params.market,
            sku = params.sku,
            purchaseToken = params.purchaseToken,
            developerPayload = params.developerPayload
        )

        if (verifyResult.isValid) {
            // Determine duration based on planId + bonusDays
            val (planType, baseDays) = when (params.planId.trim()) {
                "plan-1m", "1_MONTH" -> "1_MONTH" to 30
                "plan-3m", "3_MONTHS" -> "3_MONTHS" to 90
                "plan-6m", "6_MONTHS" -> "6_MONTHS" to 180
                else -> "3_MONTHS" to 90
            }
            val totalDays = baseDays + params.bonusDays

            // Activate / extend subscription
            val subInfo = activateSubscription(
                userId = params.userId,
                planType = planType,
                durationDays = totalDays,
                grantSource = "MARKET_${params.market.uppercase()}",
                reason = "Market IAP (${params.sku}) - bonus days: ${params.bonusDays}"
            )

            // Update transaction to VERIFIED
            suspendTransaction {
                MarketPaymentTable.update({ MarketPaymentTable.id eq txId }) {
                    it[status] = "VERIFIED"
                    it[orderId] = verifyResult.orderId ?: params.orderId
                    it[rawResponse] = verifyResult.rawResponse
                    it[updatedAt] = Clock.System.now()
                }
            }

            return VerifyMarketPurchaseResult(
                isSuccess = true,
                subscriptionInfo = subInfo,
                transactionId = txId.value.toString()
            )
        } else {
            // Update transaction to FAILED
            suspendTransaction {
                MarketPaymentTable.update({ MarketPaymentTable.id eq txId }) {
                    it[status] = "FAILED"
                    it[rawResponse] = verifyResult.rawResponse ?: verifyResult.errorMessage
                    it[updatedAt] = Clock.System.now()
                }
            }

            return VerifyMarketPurchaseResult(
                isSuccess = false,
                errorCode = "VERIFICATION_FAILED",
                errorMessage = verifyResult.errorMessage ?: "استعلام خرید از مارکت ناموفق بود"
            )
        }
    }
}

