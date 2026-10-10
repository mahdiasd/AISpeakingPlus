package ir.speaking.feature.admin.subscription.service

import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.admin.model.AdminSubscriptionGrantRequest
import ir.speaking.feature.admin.model.AdminSubscriptionItemDto
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AdminSubscriptionService {

    suspend fun getUserSubscriptions(userId: UUID): List<AdminSubscriptionItemDto> = newSuspendedTransaction(Dispatchers.IO) {
        val query = (SubscriptionTable leftJoin AdminUserTable)
            .selectAll()
            .where { SubscriptionTable.userId eq userId }
            .orderBy(SubscriptionTable.createdAt, SortOrder.DESC)

        query.map { row ->
            AdminSubscriptionItemDto(
                id = row[SubscriptionTable.id].value.toString(),
                planType = row[SubscriptionTable.planType],
                status = row[SubscriptionTable.status],
                grantSource = row[SubscriptionTable.grantSource],
                grantedBy = row[SubscriptionTable.grantedBy]?.value?.toString(),
                grantedByAdminName = row.getOrNull(AdminUserTable.fullName),
                grantReason = row[SubscriptionTable.grantReason],
                startedAt = row[SubscriptionTable.startedAt].toString(),
                expiresAt = row[SubscriptionTable.expiresAt].toString(),
                createdAt = row[SubscriptionTable.createdAt].toString()
            )
        }
    }

    suspend fun grantSubscription(
        userId: UUID,
        adminId: UUID?,
        request: AdminSubscriptionGrantRequest
    ): AdminSubscriptionItemDto = newSuspendedTransaction(Dispatchers.IO) {
        val userExists = UserTable.selectAll().where { UserTable.id eq userId }.count() > 0
        if (!userExists) throw NoSuchElementException("کاربر یافت نشد")

        val now = Clock.System.now()
        val activeSub = SubscriptionTable.selectAll()
            .where {
                (SubscriptionTable.userId eq userId) and
                (SubscriptionTable.status eq "ACTIVE") and
                (SubscriptionTable.expiresAt greater now)
            }
            .orderBy(SubscriptionTable.expiresAt, SortOrder.DESC)
            .firstOrNull()

        val baseTime = if (activeSub != null) {
            activeSub[SubscriptionTable.expiresAt]
        } else {
            now
        }

        val durationDays = if (request.durationDays <= 0) 30 else request.durationDays
        val newExpiresAt = baseTime.plus(durationDays, DateTimeUnit.DAY, kotlinx.datetime.TimeZone.UTC)

        // Do NOT mark existing active subscription(s) as EXPIRED; insert cumulative renewal row with extended expiresAt
        val insertedId = SubscriptionTable.insertAndGetId {
            it[SubscriptionTable.userId] = userId
            it[SubscriptionTable.planType] = request.planType.uppercase()
            it[SubscriptionTable.startedAt] = now
            it[SubscriptionTable.expiresAt] = newExpiresAt
            it[SubscriptionTable.status] = "ACTIVE"
            it[SubscriptionTable.grantSource] = "MANUAL_ADMIN"
            it[SubscriptionTable.grantedBy] = adminId
            it[SubscriptionTable.grantReason] = request.reason
        }

        val adminName = if (adminId != null) {
            AdminUserTable.selectAll().where { AdminUserTable.id eq adminId }.singleOrNull()?.get(AdminUserTable.fullName)
        } else null

        AdminSubscriptionItemDto(
            id = insertedId.value.toString(),
            planType = request.planType.uppercase(),
            status = "ACTIVE",
            grantSource = "MANUAL_ADMIN",
            grantedBy = adminId?.toString(),
            grantedByAdminName = adminName,
            grantReason = request.reason,
            startedAt = now.toString(),
            expiresAt = newExpiresAt.toString(),
            createdAt = now.toString()
        )
    }

    suspend fun cancelSubscription(subscriptionId: UUID): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val updated = SubscriptionTable.update(where = { SubscriptionTable.id eq subscriptionId }) {
            it[status] = "CANCELLED"
        }
        updated > 0
    }
}
