package ir.speaking.feature.admin.user.service

import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.admin.model.AdminSubscriptionItemDto
import ir.speaking.feature.admin.model.AdminUserDetailDto
import ir.speaking.feature.admin.model.AdminUserItemDto
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AdminUserService {

    fun normalizePhone(input: String): String {
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        val arabic = "٠١٢٣٤٥٦٧٨٩"
        var res = input.trim()
        for (i in 0..9) {
            res = res.replace(persian[i], '0' + i).replace(arabic[i], '0' + i)
        }
        res = res.replace(" ", "").replace("-", "")
        if (res.startsWith("+98")) res = "0" + res.substring(3)
        else if (res.startsWith("0098")) res = "0" + res.substring(4)
        else if (res.startsWith("98") && res.length == 12) res = "0" + res.substring(2)
        return res
    }

    suspend fun getUsers(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        status: String? = null
    ): Pair<Long, List<AdminUserItemDto>> = newSuspendedTransaction(Dispatchers.IO) {
        val query = UserTable.selectAll()

        if (!search.isNullOrBlank()) {
            val normalized = normalizePhone(search)
            query.andWhere {
                (UserTable.mobile like "%$normalized%") or
                (UserTable.nickName like "%$search%") or
                (UserTable.firstName like "%$search%") or
                (UserTable.lastName like "%$search%")
            }
        }

        if (!status.isNullOrBlank() && status != "ALL") {
            query.andWhere { UserTable.status eq status.uppercase() }
        }

        val total = query.count()
        val offset = ((page - 1).coerceAtLeast(0) * limit).toLong()
        val rows = query.orderBy(UserTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .toList()

        val now = Clock.System.now()
        val userIds = rows.map { it[UserTable.id].value }

        val activeSubs = if (userIds.isNotEmpty()) {
            SubscriptionTable.selectAll()
                .where {
                    (SubscriptionTable.userId inList userIds) and
                    (SubscriptionTable.status eq "ACTIVE") and
                    (SubscriptionTable.expiresAt greater now)
                }
                .orderBy(SubscriptionTable.expiresAt, SortOrder.ASC)
                .associateBy { it[SubscriptionTable.userId].value }
        } else emptyMap()

        val items = rows.map { row ->
            val uId = row[UserTable.id].value
            val sub = activeSubs[uId]
            AdminUserItemDto(
                id = uId.toString(),
                mobile = row[UserTable.mobile],
                nickName = row[UserTable.nickName],
                score = row[UserTable.score],
                avatar = row[UserTable.avatar],
                status = row[UserTable.status],
                hasActiveSubscription = sub != null,
                subscriptionExpiresAt = sub?.get(SubscriptionTable.expiresAt)?.toString(),
                createdAt = row[UserTable.createdAt].toString()
            )
        }

        Pair(total, items)
    }

    suspend fun getUserDetail(userId: UUID): AdminUserDetailDto? = newSuspendedTransaction(Dispatchers.IO) {
        val row = UserTable.selectAll().where { UserTable.id eq userId }.singleOrNull() ?: return@newSuspendedTransaction null

        val now = Clock.System.now()
        val subRow = (SubscriptionTable leftJoin AdminUserTable)
            .selectAll()
            .where {
                (SubscriptionTable.userId eq userId) and
                (SubscriptionTable.status eq "ACTIVE") and
                (SubscriptionTable.expiresAt greater now)
            }
            .orderBy(SubscriptionTable.expiresAt, SortOrder.DESC)
            .firstOrNull()

        val completedStages = StageProgressTable.selectAll()
            .where { StageProgressTable.userId eq userId }
            .count()
            .toInt()

        val activeSub = subRow?.let {
            AdminSubscriptionItemDto(
                id = it[SubscriptionTable.id].value.toString(),
                planType = it[SubscriptionTable.planType],
                status = it[SubscriptionTable.status],
                grantSource = it[SubscriptionTable.grantSource],
                grantedBy = it[SubscriptionTable.grantedBy]?.value?.toString(),
                grantedByAdminName = it.getOrNull(AdminUserTable.fullName),
                grantReason = it[SubscriptionTable.grantReason],
                startedAt = it[SubscriptionTable.startedAt].toString(),
                expiresAt = it[SubscriptionTable.expiresAt].toString(),
                createdAt = it[SubscriptionTable.createdAt].toString()
            )
        }

        AdminUserDetailDto(
            id = row[UserTable.id].value.toString(),
            mobile = row[UserTable.mobile],
            nickName = row[UserTable.nickName],
            firstName = row[UserTable.firstName],
            lastName = row[UserTable.lastName],
            gender = row[UserTable.gender],
            score = row[UserTable.score],
            avatar = row[UserTable.avatar],
            status = row[UserTable.status],
            suspendedReason = row[UserTable.suspendedReason],
            createdAt = row[UserTable.createdAt].toString(),
            updatedAt = row[UserTable.updatedAt].toString(),
            completedStagesCount = completedStages,
            activeSubscription = activeSub
        )
    }

    suspend fun updateUserStatus(userId: UUID, status: String, reason: String?): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val count = UserTable.update(where = { UserTable.id eq userId }) {
            it[UserTable.status] = status.uppercase()
            it[UserTable.suspendedReason] = reason
            it[UserTable.updatedAt] = Clock.System.now()
        }
        count > 0
    }
}
