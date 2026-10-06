package ir.speaking.feature.subscription.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.subscription.db.SubscriptionTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.*

data class UserSubscriptionInfo(
    val isSubscriber: Boolean,
    val planType: String? = null,
    val expiresAt: String? = null,
    val remainingDays: Int = 0
)

@Single
class SubscriptionRepo {

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
            .orderBy(SubscriptionTable.expiresAt to org.jetbrains.exposed.sql.SortOrder.DESC)
            .firstOrNull()

        if (activeRow != null) {
            val expiresAt = activeRow[SubscriptionTable.expiresAt]
            val remainingDays = maxOf(0, (expiresAt.epochSeconds - now.epochSeconds) / (24 * 3600)).toInt()
            UserSubscriptionInfo(
                isSubscriber = true,
                planType = activeRow[SubscriptionTable.planType],
                expiresAt = expiresAt.toString(),
                remainingDays = remainingDays
            )
        } else {
            UserSubscriptionInfo(isSubscriber = false)
        }
    }
}
