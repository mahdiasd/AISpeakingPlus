package ir.speaking.feature.subscription.db

import ir.speaking.feature.admin.db.AdminUserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PromoCodeTable : UUIDTable("promo_codes") {
    val code = varchar("code", 64).uniqueIndex()
    val title = varchar("title", 255).nullable()
    val discountType = varchar("discount_type", 32) // "PERCENTAGE" or "FIXED_AMOUNT"
    val discountValue = long("discount_value") // percent (e.g. 20) or fixed amount in Tomans (e.g. 50000)
    val maxDiscountCap = long("max_discount_cap").nullable() // maximum discount in Tomans for percentage discounts
    val minOrderAmount = long("min_order_amount").nullable() // minimum order price in Tomans
    val applicablePlans = text("applicable_plans").default("ALL") // "ALL" or comma-separated "plan-1m,plan-3m"
    val maxUses = integer("max_uses").nullable() // null = unlimited
    val usedCount = integer("used_count").default(0)
    val isOncePerUser = bool("is_once_per_user").default(true)
    val startsAt = timestamp("starts_at").nullable()
    val expiresAt = timestamp("expires_at").nullable().index()
    val isActive = bool("is_active").default(true)
    val isDeleted = bool("is_deleted").default(false)
    val createdBy = reference("created_by", AdminUserTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
