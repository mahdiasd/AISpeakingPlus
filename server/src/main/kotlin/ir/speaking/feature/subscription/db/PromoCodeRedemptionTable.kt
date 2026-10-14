package ir.speaking.feature.subscription.db

import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PromoCodeRedemptionTable : UUIDTable("promo_code_redemptions") {
    val promoCodeId = reference("promo_code_id", PromoCodeTable, onDelete = ReferenceOption.CASCADE).index()
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE).index()
    val subscriptionId = reference("subscription_id", SubscriptionTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val planId = varchar("plan_id", 64)
    val originalPrice = long("original_price")
    val discountAmount = long("discount_amount")
    val finalPrice = long("final_price")
    val redeemedAt = timestamp("redeemed_at").defaultExpression(CurrentTimestamp)
}
