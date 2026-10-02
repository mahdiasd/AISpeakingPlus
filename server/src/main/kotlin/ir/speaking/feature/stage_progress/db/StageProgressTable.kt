package ir.speaking.feature.stage_progress.db

import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object StageProgressTable : UUIDTable("stage_progress") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val stageId = reference("stage_id", StageTable, onDelete = ReferenceOption.CASCADE)
    val stars = integer("stars").check("stars_range_check") { it.between(0, 3) }
    val bestScore = integer("best_score").default(0)
    val repeatCount = integer("repeat_count").default(1)
    val completedAt = timestamp("completed_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("idx_stage_progress_user_stage", userId, stageId)
    }
}
