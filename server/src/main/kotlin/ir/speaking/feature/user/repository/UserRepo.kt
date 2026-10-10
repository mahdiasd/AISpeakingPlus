package ir.speaking.feature.user.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user.dto.DetailedUserProfileResponse
import ir.speaking.feature.user.dto.SubscriptionSummaryDto
import ir.speaking.feature.user.dto.UpdateProfileRequest
import ir.speaking.feature.user.dto.UserProfileResponse
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import java.util.*

@Single
class UserRepo(
    private val subscriptionRepo: SubscriptionRepo
) {
    constructor() : this(SubscriptionRepo())

    suspend fun findOrCreateUserByMobile(mobile: String): UserProfileResponse = suspendTransaction {
        val existing = UserTable.selectAll()
            .where { UserTable.mobile eq mobile }
            .firstOrNull()

        if (existing != null) {
            UserProfileResponse(
                id = existing[UserTable.id].value.toString(),
                phoneNumber = existing[UserTable.mobile],
                nickName = existing[UserTable.nickName],
                avatar = existing[UserTable.avatar],
                score = existing[UserTable.score]
            )
        } else {
            val newId = UserTable.insertAndGetId { row ->
                row[UserTable.mobile] = mobile
                row[UserTable.nickName] = "Learner"
                row[UserTable.avatar] = "default_avatar"
                row[UserTable.score] = 0
            }

            UserProfileResponse(
                id = newId.value.toString(),
                phoneNumber = mobile,
                nickName = "Learner",
                avatar = "default_avatar",
                score = 0
            )
        }
    }

    suspend fun getUserProfile(userId: UUID): UserProfileResponse? = suspendTransaction {
        UserTable.selectAll()
            .where { UserTable.id eq userId }
            .firstOrNull()
            ?.let { row ->
                UserProfileResponse(
                    id = row[UserTable.id].value.toString(),
                    phoneNumber = row[UserTable.mobile],
                    nickName = row[UserTable.nickName],
                    avatar = row[UserTable.avatar],
                    score = row[UserTable.score]
                )
            }
    }

    suspend fun isUserSuspended(userId: UUID): Boolean = suspendTransaction {
        UserTable.selectAll()
            .where { UserTable.id eq userId }
            .firstOrNull()
            ?.get(UserTable.status) == "SUSPENDED"
    }

    suspend fun isUserSuspendedByMobile(mobile: String): Boolean = suspendTransaction {
        UserTable.selectAll()
            .where { UserTable.mobile eq mobile }
            .firstOrNull()
            ?.get(UserTable.status) == "SUSPENDED"
    }

    suspend fun getDetailedUserProfile(userId: UUID): DetailedUserProfileResponse? {
        val userRow = suspendTransaction {
            UserTable.selectAll()
                .where { UserTable.id eq userId }
                .firstOrNull()
        } ?: return null

        val progressStats = suspendTransaction {
            val progressRows = StageProgressTable.selectAll()
                .where { StageProgressTable.userId eq userId }
                .toList()
            val totalStars = progressRows.sumOf { it[StageProgressTable.stars] }
            val completedCount = progressRows.count { it[StageProgressTable.stars] > 0 }
            Pair(totalStars, completedCount)
        }

        val subInfo = subscriptionRepo.getSubscriptionInfo(userId)
        val planTitleFa = when (subInfo.planType) {
            "1_MONTH", "MONTHLY" -> "اشتراک ۱ ماهه"
            "3_MONTHS", "QUARTERLY" -> "اشتراک ۳ ماهه"
            "6_MONTHS", "BIANNUAL", "SEMI_ANNUAL" -> "اشتراک ۶ ماهه"
            "1_YEAR", "ANNUAL", "YEARLY" -> "اشتراک ۱ ساله"
            "TRIAL" -> "اشتراک آزمایشی"
            "CUSTOM" -> "اشتراک اختصاصی"
            else -> subInfo.planType
        }

        return DetailedUserProfileResponse(
            id = userRow[UserTable.id].value.toString(),
            phoneNumber = userRow[UserTable.mobile],
            nickName = userRow[UserTable.nickName],
            firstName = userRow[UserTable.firstName],
            lastName = userRow[UserTable.lastName],
            avatar = userRow[UserTable.avatar],
            score = userRow[UserTable.score],
            totalStars = progressStats.first,
            completedStagesCount = progressStats.second,
            languageLevel = userRow[UserTable.languageLevel],
            subscription = SubscriptionSummaryDto(
                isSubscriber = subInfo.isSubscriber,
                planType = subInfo.planType,
                planTitleFa = planTitleFa,
                startedAt = subInfo.startedAt,
                expiresAt = subInfo.expiresAt,
                remainingDays = subInfo.remainingDays
            )
        )
    }

    suspend fun updateUserProfile(userId: UUID, request: UpdateProfileRequest): DetailedUserProfileResponse? {
        suspendTransaction {
            UserTable.update({ UserTable.id eq userId }) { row ->
                request.nickName?.trim()?.takeIf { it.isNotBlank() }?.let { row[nickName] = it }
                request.avatar?.trim()?.takeIf { it.isNotBlank() }?.let { row[avatar] = it }
                request.firstName?.trim()?.let { row[firstName] = it }
                request.lastName?.trim()?.let { row[lastName] = it }
                request.languageLevel?.trim()?.takeIf { it.isNotBlank() }?.let { row[languageLevel] = it.uppercase() }
                row[updatedAt] = Clock.System.now()
            }
        }
        return getDetailedUserProfile(userId)
    }
}
