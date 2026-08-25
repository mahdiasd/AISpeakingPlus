package ir.speaking.feature.purchase.db

import ir.speaking.feature.discount.db.DiscountCodeTable
import ir.speaking.feature.plan.db.PlanTable
import ir.speaking.feature.purchase.model.Purchase
import ir.speaking.feature.purchase.model.PurchaseStatus
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

object PurchaseTable : UUIDTable("purchase") {
    val userId = uuid("user_id").references(UserTable.id)
    val planId = uuid("plan_id").references(PlanTable.id)
    val discountCodeId = uuid("discount_code_id").references(DiscountCodeTable.id).nullable()
    val purchaseDate = timestamp("purchase_date")
    val expiryDate = timestamp("expiry_date").nullable()
    val amountPaid = varchar("amount_paid", 7)
    val status = enumerationByName("status", 16, PurchaseStatus::class)
    val token = varchar("token", 200)
    val createdAt = timestamp("created_at")
}

class PurchaseDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PurchaseDAO>(PurchaseTable)

    var userId by PurchaseTable.userId
    var planId by PurchaseTable.planId
    var discountCode by PurchaseTable.discountCodeId
    var purchaseDate by PurchaseTable.purchaseDate
    var expiryDate by PurchaseTable.expiryDate
    var amountPaid by PurchaseTable.amountPaid
    var status by PurchaseTable.status
    var token by PurchaseTable.token
    var createdAt by PurchaseTable.createdAt
}

fun PurchaseDAO.toModel() = Purchase(
    id = id.value,
    userId = userId,
    planId = planId,
    discountCodeId = discountCode,
    purchaseDate = purchaseDate,
    expiryDate = expiryDate,
    amountPaid = amountPaid,
    status = status,
    token = token,
    createdAt = createdAt
)

