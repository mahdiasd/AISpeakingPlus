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
                    SchemaUtils.drop(StageProgressTable, SubscriptionTable, StageTable, UserTable)
                }
                SchemaUtils.create(UserTable, StageTable, StageProgressTable, SubscriptionTable)

                if (StageTable.selectAll().empty()) {
                    for (stage in StageSeedData.stages) {
                        StageTable.insert {
                            it[id] = stage.id
                            it[orderIndex] = stage.orderIndex
                            it[title] = stage.title
                            it[titleFa] = stage.titleFa
                            it[briefing] = stage.briefing
                            it[briefingFa] = stage.briefingFa
                            it[targetObjective] = stage.targetObjective
                            it[backgroundUrl] = stage.backgroundUrl
                            it[characterName] = stage.characterName
                            it[characterAvatarUrl] = stage.characterAvatarUrl
                            it[characterGender] = stage.characterGender
                            it[voiceId] = stage.voiceId
                            it[initialSpeaker] = stage.initialSpeaker
                            it[maxTurns] = stage.maxTurns
                        }
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
