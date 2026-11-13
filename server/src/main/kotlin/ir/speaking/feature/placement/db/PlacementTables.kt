package ir.speaking.feature.placement.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PlacementQuestionsTable : Table("placement_questions") {
    val id = varchar("id", 64)
    val questionText = text("question_text")
    val optionsJson = text("options_json")
    val correctOption = varchar("correct_option", 255)
    val level = varchar("level", 16).index() // A1, A2, B1, B2, C1
    val category = varchar("category", 64).default("grammar")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object PlacementSessionsTable : Table("placement_sessions") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val questionIdsJson = text("question_ids_json")
    val scorePercent = integer("score_percent").default(0)
    val assignedLevel = varchar("assigned_level", 16).nullable()
    val correctCount = integer("correct_count").default(0)
    val wrongCount = integer("wrong_count").default(0)
    val isCompleted = bool("is_completed").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val completedAt = timestamp("completed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
