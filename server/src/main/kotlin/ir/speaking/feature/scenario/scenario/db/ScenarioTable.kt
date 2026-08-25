package ir.speaking.feature.scenario.scenario.db


import ir.speaking.feature.category.db.CategoryTable
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.scenario.scenario.model.Scenario
import ir.speaking.feature.user.model.Gender
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object ScenarioTable : UUIDTable("scenario") {
    var categoryId = uuid("category_id").references(CategoryTable.id)
    val title = varchar("title", 255)
    val persianTitle = varchar("persian_title", 255)
    val description = text("description")
    val persianDescription = text("persian_description")
    val aiRole = varchar("ai_role", 255)
    val gender = enumerationByName("ai_gender", 5, Gender::class)
    val imageUrl = varchar("image_url", 255).nullable()
    val aiName = varchar("ai_name", 100)
    val aiAvatar = varchar("ai_avatar", 255).nullable()
    val points = integer("points")
    val createdAt = datetime("created_at")
    val starter = enumerationByName("starter", 10, Role::class)
}

class ScenarioDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScenarioDAO>(ScenarioTable)

    var categoryId by ScenarioTable.categoryId
    var title by ScenarioTable.title
    var persianTitle by ScenarioTable.persianTitle
    var persianDescription by ScenarioTable.persianDescription
    var description by ScenarioTable.description
    var imageUrl by ScenarioTable.imageUrl
    var aiName by ScenarioTable.aiName
    var aiAvatar by ScenarioTable.aiAvatar
    var points by ScenarioTable.points
    var aiRole by ScenarioTable.aiRole
    var gender by ScenarioTable.gender
    var createdAt by ScenarioTable.createdAt
    var starter by ScenarioTable.starter
}

fun ScenarioDAO.toModel() = Scenario(
    id = id.value,
    categoryId = categoryId,
    title = title,
    description = description,
    imageUrl = imageUrl,
    aiName = aiName,
    aiAvatar = aiAvatar,
    points = points,
    persianDescription = persianDescription,
    aiRole = aiRole,
    gender = gender,
    persianTitle = persianTitle,
    createdAt = createdAt,
    starter = starter
)
