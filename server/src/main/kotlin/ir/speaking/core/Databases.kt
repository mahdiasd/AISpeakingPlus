package ir.speaking.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.feature.admin.db.AdminAuditTable
import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.stage.db.StageSeedData
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.subscription.db.PromoCodeRedemptionTable
import ir.speaking.feature.subscription.db.MarketPaymentTable
import ir.speaking.feature.subscription.db.PromoCodeTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.announcement.db.AnnouncementTable
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
    AdminAuditTable,
    PromoCodeTable,
    PromoCodeRedemptionTable,
    MarketPaymentTable,
    AnnouncementTable
)

fun createHikariDataSource(): HikariDataSource {
    val dbUrl = System.getenv("DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5432/ai_speaking"
    val dbUser = System.getenv("DATABASE_USER") ?: "postgres"
    val dbPassword = System.getenv("DATABASE_PASSWORD") ?: ""

    val config = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        driverClassName = if (dbUrl.contains("h2")) "org.h2.Driver" else "org.postgresql.Driver"
        maximumPoolSize = System.getenv("DB_POOL_MAX_SIZE")?.toIntOrNull() ?: 15
        minimumIdle = System.getenv("DB_POOL_MIN_IDLE")?.toIntOrNull() ?: 3
        idleTimeout = 300000L
        maxLifetime = 1200000L
        connectionTimeout = 10000L
        leakDetectionThreshold = 30000L
        poolName = "AISpeakingHikariPool"

        if (!dbUrl.contains("h2")) {
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }
    }
    return HikariDataSource(config)
}

fun configureDatabases() {
    try {
        val dataSource = createHikariDataSource()
        Database.connect(dataSource)

        transaction {
            if (allTables.isNotEmpty()) {
                SchemaUtils.createMissingTablesAndColumns(*allTables)
            }

            try {
                exec("ALTER TABLE stages ADD COLUMN IF NOT EXISTS target_objective_fa TEXT NOT NULL DEFAULT '';")
                exec("ALTER TABLE stages ADD COLUMN IF NOT EXISTS character_behavior TEXT;")
            } catch (e: Exception) {
                PrintHelper.warning("Stage table migration notice: ${e.message}")
            }

            seedOrSyncStages()
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
        val existing = existingById ?: existingByOrder
        if (existing == null) {
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
        } else {
            val currentVoiceId = existing[StageTable.voiceId]
            val currentGender = existing[StageTable.characterGender]
            val currentAvatar = existing[StageTable.characterAvatarUrl]
            val currentBehavior = existing[StageTable.characterBehavior]
            val currentBackground = existing[StageTable.backgroundUrl]
            if (currentVoiceId != stage.voiceId ||
                currentGender != stage.characterGender ||
                currentAvatar != stage.characterAvatarUrl ||
                currentBehavior != stage.characterBehavior ||
                currentBackground != stage.backgroundUrl
            ) {
                val targetId = existing[StageTable.id]
                StageTable.update({ StageTable.id eq targetId }) {
                    it[voiceId] = stage.voiceId
                    it[characterGender] = stage.characterGender
                    it[characterAvatarUrl] = stage.characterAvatarUrl
                    it[characterBehavior] = stage.characterBehavior
                    it[backgroundUrl] = stage.backgroundUrl
                }
            }
        }
    }
}
