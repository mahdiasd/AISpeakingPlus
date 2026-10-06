package ir.speaking.feature.stage.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object StageTable : IdTable<String>("stages") {
    override val id: Column<EntityID<String>> = varchar("id", 64).entityId()
    override val primaryKey = PrimaryKey(id)

    val orderIndex = integer("order_index").uniqueIndex()
    val title = varchar("title", 255)
    val titleFa = varchar("title_fa", 255)
    val briefing = text("briefing")
    val briefingFa = text("briefing_fa")
    val targetObjective = text("target_objective").default("")
    val targetObjectiveFa = text("target_objective_fa").default("")
    val characterBehavior = text("character_behavior").nullable()
    val backgroundUrl = varchar("background_url", 512)
    val characterName = varchar("character_name", 128)
    val characterAvatarUrl = varchar("character_avatar_url", 512).nullable()
    val characterGender = varchar("character_gender", 16).default("Woman")
    val voiceId = varchar("voice_id", 64).nullable()
    val initialSpeaker = varchar("initial_speaker", 16).default("Model")
    val maxTurns = integer("max_turns").default(12)
    val status = varchar("status", 32).default("PUBLISHED").index()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
