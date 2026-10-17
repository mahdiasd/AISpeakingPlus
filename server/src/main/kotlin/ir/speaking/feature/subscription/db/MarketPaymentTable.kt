package ir.speaking.feature.subscription.db

import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MarketPaymentTable : UUIDTable("market_payment_transactions") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE).index("idx_market_tx_user")
    val market = varchar("market", 32)
    val sku = varchar("sku", 64)
    val purchaseToken = text("purchase_token").uniqueIndex("idx_market_tx_token")
    val developerPayload = text("developer_payload").nullable()
    val orderId = varchar("order_id", 128).nullable()
    val amountPaidTomans = long("amount_paid_tomans").default(0L)
    val promoCode = varchar("promo_code", 64).nullable()
    val bonusDays = integer("bonus_days").default(0)
    val status = varchar("status", 32).default("PENDING")
    val rawResponse = text("raw_response").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
