package ir.speaking.feature.plan.db

import ir.speaking.feature.plan.model.Plan
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

object PlanTable : UUIDTable("plan") {
    val title = varchar("title", 50)
    val name = varchar("name", 50)
    val description = text("description")
    val visibility = bool("visibility")
    val price = varchar("price", 7)
    val discountedPrice = varchar("discounted_price", 7).nullable()
    val cafeBazaarId = varchar("cafe_bazaar_id", 100)
    val dayDuration = integer("day_duration")
    val createdAt = timestamp("created_at")
}

class PlanDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PlanDAO>(PlanTable)

    var title by PlanTable.title
    var name by PlanTable.name
    var description by PlanTable.description
    var price by PlanTable.price
    var discountedPrice by PlanTable.discountedPrice
    var cafeBazaarId by PlanTable.cafeBazaarId
    var dayDuration by PlanTable.dayDuration
    var visibility by PlanTable.visibility
    var createdAt by PlanTable.createdAt
}

fun PlanDAO.toModel() = Plan(
    id = id.value,
    title = title,
    description = description,
    price = price,
    discountedPrice = discountedPrice,
    dayDuration = dayDuration,
    cafeBazaarId = cafeBazaarId,
    visibility = visibility,
    name = name,
    createdAt = createdAt
)
