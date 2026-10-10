package ir.speaking.core

import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.feature.admin.db.AdminAuditTable
import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.stage.db.StageSeedData
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

private val allTables = arrayOf<Table>(
    UserTable,
    StageTable,
    StageProgressTable,
    SubscriptionTable,
    AdminUserTable,
    AdminAuditTable
)

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
            try {
                exec("ALTER TABLE stages ADD COLUMN IF NOT EXISTS target_objective_fa TEXT NOT NULL DEFAULT '';")
                exec("ALTER TABLE stages ADD COLUMN IF NOT EXISTS character_behavior TEXT;")
            } catch (e: Exception) {
                PrintHelper.warning("Stage table migration notice: ${e.message}")
            }

            if (allTables.isNotEmpty()) {
                SchemaUtils.createMissingTablesAndColumns(*allTables)
            }
            seedStagesIfEmpty()
            seedSuperAdminIfEmpty()
        }
    } catch (e: Exception) {
        PrintHelper.error(e.message.toString())
    }
}

fun seedSuperAdminIfEmpty() {
    if (AdminUserTable.selectAll().empty()) {
        val email = System.getenv("ADMIN_INITIAL_EMAIL") ?: "admin@aispeaking.ir"
        val password = System.getenv("ADMIN_INITIAL_PASSWORD") ?: "Admin@123456!"
        val hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, password.toCharArray())
        AdminUserTable.insert {
            it[username] = email
            it[passwordHash] = hash
            it[fullName] = "Super Admin"
            it[role] = "ROLE_SUPER_ADMIN"
            it[isActive] = true
        }
        PrintHelper.info("Initial SuperAdmin created: $email")
    }
}

fun seedStagesIfEmpty() {
    if (StageTable.selectAll().empty()) {
        seedOrSyncStages()
    }
}

fun seedOrSyncStages() {
    for (stage in StageSeedData.stages) {
        val existingById = StageTable.selectAll().where { StageTable.id eq stage.id }.firstOrNull()
        val existingByOrder = StageTable.selectAll().where { StageTable.orderIndex eq stage.orderIndex }.firstOrNull()
        if (existingById == null && existingByOrder == null) {
            StageTable.insert {
                it[id] = stage.id
                it[orderIndex] = stage.orderIndex
                it[title] = stage.title
                it[titleFa] = stage.titleFa
                it[briefing] = stage.briefing
                it[briefingFa] = stage.briefingFa
                it[targetObjective] = stage.targetObjective
                it[targetObjectiveFa] = stage.targetObjectiveFa
                it[characterBehavior] = stage.characterBehavior
                it[backgroundUrl] = stage.backgroundUrl
                it[characterName] = stage.characterName
                it[characterAvatarUrl] = stage.characterAvatarUrl
                it[characterGender] = stage.characterGender
                it[voiceId] = stage.voiceId
                it[initialSpeaker] = stage.initialSpeaker
                it[maxTurns] = stage.maxTurns
                it[status] = "PUBLISHED"
            }
        }
    }
}
