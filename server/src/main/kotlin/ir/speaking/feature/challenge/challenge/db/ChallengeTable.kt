package ir.speaking.feature.challenge.challenge.db

import ir.speaking.feature.challenge.challenge.model.Challenge
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.user.model.Gender
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object ChallengeTable : UUIDTable("challenge") {
    val title = varchar("title", 255)
    val persianTitle = varchar("persian_title", 255)
    val description = text("description")
    val persianDescription = text("persian_description")
    val aiRole = varchar("ai_role", 255)
    val imageUrl = varchar("image_url", 255).nullable()
    val aiName = varchar("ai_name", 100)
    val gender = enumerationByName("ai_gender", 10, Gender::class)
    val aiAvatar = varchar("ai_avatar", 255).nullable()
    val points = integer("points")
    val createdAt = datetime("created_at")
    val starter = enumerationByName("starter", 10, Role::class)

    val startDate = date("start_date")
    val endDate = date("end_date")
}

class ChallengeDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ChallengeDAO>(ChallengeTable)

    var title by ChallengeTable.title
    var persianTitle by ChallengeTable.persianTitle
    var persianDescription by ChallengeTable.persianDescription
    var description by ChallengeTable.description
    var imageUrl by ChallengeTable.imageUrl
    var aiName by ChallengeTable.aiName
    var aiAvatar by ChallengeTable.aiAvatar
    var points by ChallengeTable.points
    var aiRole by ChallengeTable.aiRole
    var startDate by ChallengeTable.startDate
    var endDate by ChallengeTable.endDate
    var gender by ChallengeTable.gender
    var starter by ChallengeTable.starter
    var createdAt by ChallengeTable.createdAt
}

fun ChallengeDAO.toModel() = Challenge(
    uid = id.value,
    title = title,
    description = description,
    imageUrl = imageUrl,
    aiName = aiName,
    aiAvatar = aiAvatar,
    points = points,
    persianDescription = persianDescription,
    aiRole = aiRole,
    persianTitle = persianTitle,
    gender = gender,
    starter = starter,
    createdAt = createdAt,
    startDate = startDate,
    endDate = endDate,
)