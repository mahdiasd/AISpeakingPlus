package ir.aispeaking.domain.model.payment

import kotlinx.serialization.Serializable

@Serializable
enum class AppMarket(val id: String, val titleFa: String) {
    CAFE_BAZAAR("CAFE_BAZAAR", "کافه بازار"),
    MYKET("MYKET", "مایکت"),
    DIRECT_WEB("DIRECT_WEB", "پرداخت مستقیم"),
    UNSUPPORTED("UNSUPPORTED", "پلتفرم پشتیبانی‌نشده");

    companion object {
        fun fromId(id: String?): AppMarket {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: UNSUPPORTED
        }
    }
}

@Serializable
sealed interface MarketPurchaseResult {
    @Serializable
    data class Success(
        val purchaseToken: String,
        val orderId: String? = null,
        val sku: String,
        val developerPayload: String? = null,
        val market: AppMarket
    ) : MarketPurchaseResult

    @Serializable
    data class UserCancelled(val message: String = "خرید توسط کاربر لغو شد") : MarketPurchaseResult

    @Serializable
    data class Error(val errorCode: Int? = null, val message: String) : MarketPurchaseResult
}
