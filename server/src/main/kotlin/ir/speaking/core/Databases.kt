package ir.speaking.core

import ir.speaking.core.network.utils.PrintHelper
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

private val allTables = arrayOf<Table>()

fun configureDatabases() {
    try {
        val dbUrl = System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://localhost:5432/ai_speaking"
        val dbUser = System.getenv("DATABASE_USER") ?: "postgres"
        val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "asdfasWERASDCASDF89845@af"

        Database.connect(
            url = dbUrl,
            user = dbUser,
            password = dbPassword
        )

        transaction {
            if (allTables.isNotEmpty()) {
                SchemaUtils.createMissingTablesAndColumns(*allTables)
            }
        }
    } catch (e: Exception) {
        PrintHelper.error(e.message.toString())
    }
}
