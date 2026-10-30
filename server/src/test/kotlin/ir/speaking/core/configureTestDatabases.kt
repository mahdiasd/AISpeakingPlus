package ir.speaking.core

import io.ktor.server.application.*
import ir.speaking.feature.stage.db.StageSeedData
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

internal fun Application.configureTestDatabases(dropTables: Boolean = false) {
    val dbUrl = System.getenv("TEST_DATABASE_URL")
        ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
    val dbUser = System.getenv("TEST_DATABASE_USER") ?: "sa"
    val dbPassword = System.getenv("TEST_DATABASE_PASSWORD") ?: ""

    try {
        Database.connect(
            dbUrl,
            driver = if (dbUrl.contains("h2")) "org.h2.Driver" else "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword
        )
        try {
            transaction {
                if (dropTables) {
                    SchemaUtils.drop(
                        StageProgressTable,
                        SubscriptionTable,
                        StageTable,
                        UserTable,
                        ir.speaking.feature.admin.db.AdminAuditTable,
                        ir.speaking.feature.admin.db.AdminUserTable,
                        ir.speaking.feature.subscription.db.PromoCodeRedemptionTable,
                        ir.speaking.feature.subscription.db.PromoCodeTable,
                        ir.speaking.feature.subscription.db.MarketPaymentTable,
                        ir.speaking.feature.announcement.db.AnnouncementTable
                    )
                }
                SchemaUtils.create(
                    UserTable,
                    StageTable,
                    StageProgressTable,
                    SubscriptionTable,
                    ir.speaking.feature.admin.db.AdminUserTable,
                    ir.speaking.feature.admin.db.AdminAuditTable,
                    ir.speaking.feature.subscription.db.PromoCodeTable,
                    ir.speaking.feature.subscription.db.PromoCodeRedemptionTable,
                    ir.speaking.feature.subscription.db.MarketPaymentTable,
                    ir.speaking.feature.announcement.db.AnnouncementTable
                )
                seedSuperAdminIfEmpty()
                seedOrSyncStages()
                if (ir.speaking.feature.subscription.db.PromoCodeTable.selectAll().where { ir.speaking.feature.subscription.db.PromoCodeTable.code eq "GOLD20" }.empty()) {
                    ir.speaking.feature.subscription.db.PromoCodeTable.insert {
                        it[code] = "GOLD20"
                        it[title] = "Test Promo Code"
                        it[discountType] = "PERCENTAGE"
                        it[discountValue] = 20
                        it[applicablePlans] = "ALL"
                        it[isOncePerUser] = false
                        it[isActive] = true
                        it[isDeleted] = false
                    }
                }
                if (ir.speaking.feature.subscription.db.PromoCodeTable.selectAll().where { ir.speaking.feature.subscription.db.PromoCodeTable.code eq "FREE100" }.empty()) {
                    ir.speaking.feature.subscription.db.PromoCodeTable.insert {
                        it[code] = "FREE100"
                        it[title] = "100% Free Promo Code"
                        it[discountType] = "PERCENTAGE"
                        it[discountValue] = 100
                        it[applicablePlans] = "ALL"
                        it[isOncePerUser] = false
                        it[isActive] = true
                        it[isDeleted] = false
                    }
                }
            }
        } catch (e: Exception) {
            println("Test DB setup failed: ${e.message}")
        }
    } catch (e: Exception) {
        println("Database connect failed: ${e.message}")
    }
}
