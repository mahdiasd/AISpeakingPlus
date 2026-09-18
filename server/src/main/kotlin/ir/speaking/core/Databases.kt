package ir.speaking.core

import ir.speaking.admin.admin.db.AdminTable
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user_device_info.db.UserDeviceInfoTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val allTables = arrayOf(
    AdminTable,
    UserTable,
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

