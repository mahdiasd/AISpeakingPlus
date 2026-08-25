package ir.speaking.core

import io.ktor.server.application.*
import ir.speaking.feature.category.db.CategoryTable
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.scenario.task.db.ScenarioTaskTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal fun Application.configureTestDatabases(dropTables: Boolean = false) {
    val db = Database.connect(
        "jdbc:postgresql://localhost:5432/ai_speaking_test",
        user = "postgres",
        password = "123456",
    )


    transaction {
        if (dropTables) {
            SchemaUtils.drop(UserTable, CategoryTable, ScenarioTable, ScenarioTaskTable)
        }
        SchemaUtils.create(UserTable)
        SchemaUtils.create(CategoryTable)
        SchemaUtils.create(ScenarioTable)
        SchemaUtils.create(ScenarioTaskTable)
    }
}


