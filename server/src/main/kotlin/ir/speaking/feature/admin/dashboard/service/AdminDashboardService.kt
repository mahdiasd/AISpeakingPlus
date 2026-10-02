package ir.speaking.feature.admin.dashboard.service

import ir.speaking.feature.admin.model.AdminDashboardStatsDto
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class AdminDashboardService {

    suspend fun getStats(): AdminDashboardStatsDto = newSuspendedTransaction(Dispatchers.IO) {
        val now = Clock.System.now()
        val totalUsers = UserTable.selectAll().count()
        val activeUsers = UserTable.selectAll().where { UserTable.status eq "ACTIVE" }.count()
        val activeSubscriptions = SubscriptionTable.selectAll()
            .where {
                (SubscriptionTable.status eq "ACTIVE") and
                (SubscriptionTable.expiresAt greater now)
            }
            .count()
        val totalStages = StageTable.selectAll().count()
        val publishedStages = StageTable.selectAll().where { StageTable.status eq "PUBLISHED" }.count()

        AdminDashboardStatsDto(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            activeSubscriptions = activeSubscriptions,
            totalStages = totalStages,
            publishedStages = publishedStages
        )
    }
}
