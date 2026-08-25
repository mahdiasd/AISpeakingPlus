package ir.speaking.feature.discount.db

import ir.speaking.core.utils.toUUIDOrNull
import ir.speaking.feature.discount.model.DiscountCode
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object DiscountCodeTable : UUIDTable("discount_code") {
    val code = varchar("code", 12).uniqueIndex()
    val percentage = integer("percentage")
    val isActive = bool("is_active")
    val expiryDate = datetime("expiry_date")
    val createdAt = datetime("created_at")
    val applicablePlans = text("applicable_plans") // JSON string of plan IDs
}

class DiscountCodeDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DiscountCodeDAO>(DiscountCodeTable)

    var code by DiscountCodeTable.code
    var percentage by DiscountCodeTable.percentage
    var isActive by DiscountCodeTable.isActive
    var expiryDate by DiscountCodeTable.expiryDate
    var createdAt by DiscountCodeTable.createdAt
    var applicablePlans by DiscountCodeTable.applicablePlans
}

fun DiscountCodeDAO.toModel(): DiscountCode {
    val planIds = applicablePlans.split(",").mapNotNull { it.trim().toUUIDOrNull() }
    return DiscountCode(
        id = id.value,
        code = code,
        percentage = percentage,
        isActive = isActive,
        expiryDate = expiryDate,
        createdAt = createdAt,
        applicablePlans = planIds
    )
}
