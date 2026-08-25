package ir.speaking.core.data_provider.discount

import ir.speaking.core.utils.now
import ir.speaking.feature.discount.db.DiscountCodeTable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun populateDiscount() {
    transaction {
        DiscountCodeTable.insert {
            it[code] = "CAMPAIGN_60"
            it[percentage] = 60
            it[isActive] = true
            it[expiryDate] = LocalDateTime.now().toJavaLocalDateTime().plusMonths(12).toKotlinLocalDateTime()
            it[applicablePlans] = ""
            it[createdAt] = LocalDateTime.now()
        }
    }
}
