package ir.speaking.core

import ir.speaking.admin.admin.db.AdminTable
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.feature.category.db.CategoryTable
import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.progress.db.ChallengeProgressTable
import ir.speaking.feature.challenge.task.db.ChallengeTaskTable
import ir.speaking.feature.discount.db.DiscountCodeTable
import ir.speaking.feature.lightener.db.TranslationTable
import ir.speaking.feature.plan.db.PlanTable
import ir.speaking.feature.purchase.db.PurchaseTable
import ir.speaking.feature.scenario.progress.db.ScenarioProgressTable
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.scenario.task.db.ScenarioTaskTable
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user_device_info.db.UserDeviceInfoTable
import ir.speaking.feature.word.progress.db.WordProgressTable
import ir.speaking.feature.word.word.DailyWordTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val allTables = arrayOf(
    AdminTable,
    UserTable,
    CategoryTable,
    ScenarioTable,
    ScenarioTaskTable,
    ScenarioProgressTable,
    PurchaseTable,
    PlanTable,
    DiscountCodeTable,
    TranslationTable,
    DailyWordTable,
    WordProgressTable,
    ChallengeTable,
    ChallengeTaskTable,
    ChallengeProgressTable,
    UserDeviceInfoTable
)

fun configureDatabases() {
    try {
        val dbUrl = System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://localhost:5432/ai_speaking"  // fallback برای محلی بدون Docker
        val dbUser = System.getenv("DATABASE_USER") ?: "postgres"
        val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "asdfasWERASDCASDF89845@af"

       val db =  Database.connect(
            url = dbUrl,
            user = dbUser,
            password = dbPassword
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(*allTables)
        }
    }catch (e : Exception)
    {
        PrintHelper.error(e.message.toString())
    }

}

