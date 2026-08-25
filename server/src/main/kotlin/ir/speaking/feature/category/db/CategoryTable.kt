package ir.speaking.feature.category.db


import ir.speaking.feature.category.model.Category
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object CategoryTable : UUIDTable("category") {
    val name = varchar("name", 100)
    val imageUrl = varchar("image_url", 255).nullable()
    val createdAt = datetime("created_at")
}

class CategoryDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CategoryDAO>(CategoryTable)

    var name by CategoryTable.name
    var imageUrl by CategoryTable.imageUrl
    var createdAt by CategoryTable.createdAt
}

fun CategoryDAO.toModel() = Category(
    id = id.value,
    name = name,
    imageUrl = imageUrl,
    createdAt = createdAt
)